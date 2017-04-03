package com.mateyinc.marko.matey.activity.register;


import android.os.Bundle;

/**
 * Used by fragment to notify activity for changes.
 * {@link RegisterActivity} will implement this interface.
 */
 interface FragmentListener {
    /**
     * Called by fragment when the user has finished entering valid inputs and need to go to the next registration step.
     * @param b {@link Bundle} object which contains inputs entered by user.
     * @param fragPos position of fragment in registration process.
     */
    void onNext(Bundle b, int fragPos);

//    /**
//     * Called by fragment when user wants to go one step back in registration process.
//     * @param fragPos position of fragment in reg process.
//     */
//    void onBack(int fragPos);
//
//    /**
//     * Called when user wants to stop the registration process.
//     */
//    void onCancel();
}
