# Admin Cleanup Note: Leaderboard Shows Deleted Users

## Root Cause

Firebase Authentication and Cloud Firestore are **completely independent systems**. Deleting a user account in Firebase Authentication — whether through the Firebase Console, `FirebaseAuth.currentUser.delete()`, or the Admin SDK — removes **only** the Auth record.

The corresponding Firestore document at `users/{uid}` (and its `quizScores/{levelId}` subcollection) is **not touched**. Those documents persist indefinitely and will continue to appear in the leaderboard query:

```
db.collection("users")
  .orderBy("totalPoints", DESCENDING)
  .limit(10)
```

This is the root cause of the "deleted users still appear in the leaderboard" bug.

---

## Fix Options

### Option 1 — Cloud Function trigger (recommended for production)

Deploy a Firebase Cloud Function that listens to the `auth.user().onDelete()` event and deletes the matching Firestore data atomically:

```js
// functions/index.js
const { onRequest } = require("firebase-functions/v2/https");
const { onDocumentDeleted } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");
admin.initializeApp();

exports.deleteUserData = functions.auth.user().onDelete(async (user) => {
  const uid = user.uid;
  const db = admin.firestore();
  const userRef = db.collection("users").doc(uid);

  // Delete quizScores subcollection
  const scores = await userRef.collection("quizScores").get();
  const batch = db.batch();
  scores.docs.forEach(doc => batch.delete(doc.ref));
  batch.delete(userRef);
  await batch.commit();

  console.log(`Deleted Firestore data for deleted Auth user: ${uid}`);
});
```

Deploy with:
```bash
firebase deploy --only functions
```

> **This is the only option that automatically keeps Auth and Firestore in sync going forward.**

---

### Option 2 — Admin SDK script (one-off cross-check)

If Cloud Functions aren't available, run a Node.js script with the Admin SDK to list all Auth UIDs and delete any Firestore `users/{uid}` document that doesn't have a matching live Auth account:

```js
// scripts/cleanup_orphans.js
const admin = require("firebase-admin");
const serviceAccount = require("./serviceAccountKey.json");
admin.initializeApp({ credential: admin.credential.cert(serviceAccount) });

async function cleanupOrphans() {
  const db = admin.firestore();
  const usersSnap = await db.collection("users").get();

  for (const userDoc of usersSnap.docs) {
    try {
      await admin.auth().getUser(userDoc.id);
      // Auth account exists — keep the Firestore doc
    } catch (e) {
      if (e.code === "auth/user-not-found") {
        // Orphaned doc — delete it and its subcollections
        const scoresSnap = await userDoc.ref.collection("quizScores").get();
        const batch = db.batch();
        scoresSnap.docs.forEach(s => batch.delete(s.ref));
        batch.delete(userDoc.ref);
        await batch.commit();
        console.log("Deleted orphaned user doc:", userDoc.id);
      }
    }
  }
}

cleanupOrphans().catch(console.error);
```

Run with:
```bash
node scripts/cleanup_orphans.js
```

---

### Option 3 — One-time full wipe (current situation: all accounts deleted)

Because **all** Auth accounts have already been deleted, use the `deleteAllUsersData()` function in [`FirestoreRepository.kt`](../app/src/main/java/com/adam/ecolens/data/repository/FirestoreRepository.kt):

```kotlin
// Call from a debug button in a debug-only Activity or Fragment:
lifecycleScope.launch {
    try {
        firestoreRepository.deleteAllUsersData()
        Toast.makeText(this@DebugActivity, "All user data wiped.", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(this@DebugActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
```

> ⚠️ This deletes **all** documents in the `users` collection and every `quizScores` subcollection. It is irreversible. Only use it when you have confirmed that the `users` collection contains only orphaned data.

---

## Going Forward

To avoid this issue recurring, **always delete users through both systems together**:

| System | Action |
|---|---|
| Firebase Auth | Delete via Console → Authentication, or call `user.delete()` / Admin SDK |
| Firestore | Delete `users/{uid}` and all subcollections manually, via Admin SDK, or via a Cloud Function trigger |

The cleanest long-term solution is **Option 1** (Cloud Function trigger), which makes deletion automatic and transactional.
