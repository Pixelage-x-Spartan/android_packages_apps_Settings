/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.biometrics2.factory;

import android.app.Application;
import android.app.KeyguardManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory;
import androidx.lifecycle.viewmodel.CreationExtras;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.biometrics.fingerprint.FingerprintUpdater;
import com.android.settings.biometrics2.data.repository.AccessibilityRepository;
import com.android.settings.biometrics2.data.repository.FingerprintRepository;
import com.android.settings.biometrics2.data.repository.VibratorRepository;
import com.android.settings.biometrics2.ui.model.EnrollmentRequest;
import com.android.settings.biometrics2.ui.viewmodel.AutoCredentialViewModel;
import com.android.settings.biometrics2.ui.viewmodel.AutoCredentialViewModel.ChallengeGenerator;
import com.android.settings.biometrics2.ui.viewmodel.DeviceFoldedViewModel;
import com.android.settings.biometrics2.ui.viewmodel.DeviceRotationViewModel;
import com.android.settings.biometrics2.ui.viewmodel.FingerprintEnrollEnrollingViewModel;
import com.android.settings.biometrics2.ui.viewmodel.FingerprintEnrollFindSensorViewModel;
import com.android.settings.biometrics2.ui.viewmodel.FingerprintEnrollIntroViewModel;
import com.android.settings.biometrics2.ui.viewmodel.FingerprintEnrollProgressViewModel;
import com.android.settings.biometrics2.ui.viewmodel.FingerprintEnrollmentViewModel;
import com.android.settings.overlay.FeatureFactory;
import com.android.systemui.unfold.compat.ScreenSizeFoldProvider;

/**
 * View model factory for biometric enrollment fragment
 */
public class BiometricsViewModelFactory implements ViewModelProvider.Factory {

    private static final String TAG = "BiometricsViewModelFact";

    public static final CreationExtras.Key<ChallengeGenerator> CHALLENGE_GENERATOR_KEY =
            new CreationExtras.Key<ChallengeGenerator>() {};
    public static final CreationExtras.Key<EnrollmentRequest> ENROLLMENT_REQUEST_KEY =
            new CreationExtras.Key<EnrollmentRequest>() {};
    public static final CreationExtras.Key<Integer> USER_ID_KEY =
            new CreationExtras.Key<Integer>() {};

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass,
            @NonNull CreationExtras extras) {
        final Application application = extras.get(AndroidViewModelFactory.APPLICATION_KEY);

        if (application == null) {
            Log.w(TAG, "create, null application");
            return create(modelClass);
        }
        final FeatureFactory featureFactory = FeatureFactory.getFactory(application);
        final BiometricsRepositoryProvider provider = FeatureFactory.getFactory(application)
                .getBiometricsRepositoryProvider();

        if (modelClass.isAssignableFrom(AutoCredentialViewModel.class)) {
            final LockPatternUtils lockPatternUtils =
                    featureFactory.getSecurityFeatureProvider().getLockPatternUtils(application);
            final ChallengeGenerator challengeGenerator = extras.get(CHALLENGE_GENERATOR_KEY);
            if (challengeGenerator != null) {
                return (T) new AutoCredentialViewModel(application, lockPatternUtils,
                        challengeGenerator);
            }
        } else if (modelClass.isAssignableFrom(DeviceFoldedViewModel.class)) {
            return (T) new DeviceFoldedViewModel(new ScreenSizeFoldProvider(application),
                    application.getMainExecutor());
        } else if (modelClass.isAssignableFrom(DeviceRotationViewModel.class)) {
            return (T) new DeviceRotationViewModel(application);
        } else if (modelClass.isAssignableFrom(FingerprintEnrollFindSensorViewModel.class)) {
            final EnrollmentRequest request = extras.get(ENROLLMENT_REQUEST_KEY);
            if (request != null) {
                return (T) new FingerprintEnrollFindSensorViewModel(application, request.isSuw());
            }
        } else if (modelClass.isAssignableFrom(FingerprintEnrollIntroViewModel.class)) {
            final FingerprintRepository repository = provider.getFingerprintRepository(application);
            final EnrollmentRequest request = extras.get(ENROLLMENT_REQUEST_KEY);
            final Integer userId = extras.get(USER_ID_KEY);
            if (repository != null && request != null && userId != null) {
                return (T) new FingerprintEnrollIntroViewModel(application, repository, request,
                        userId);
            }
        } else if (modelClass.isAssignableFrom(FingerprintEnrollmentViewModel.class)) {
            final FingerprintRepository repository = provider.getFingerprintRepository(application);
            final EnrollmentRequest request = extras.get(ENROLLMENT_REQUEST_KEY);
            if (repository != null && request != null) {
                return (T) new FingerprintEnrollmentViewModel(application, repository,
                        application.getSystemService(KeyguardManager.class), request);
            }
        } else if (modelClass.isAssignableFrom(FingerprintEnrollProgressViewModel.class)) {
            final Integer userId = extras.get(USER_ID_KEY);
            if (userId != null) {
                return (T) new FingerprintEnrollProgressViewModel(application,
                        new FingerprintUpdater(application), userId);
            }
        } else if (modelClass.isAssignableFrom(FingerprintEnrollEnrollingViewModel.class)) {
            final FingerprintRepository fingerprint = provider.getFingerprintRepository(
                    application);
            final AccessibilityRepository accessibility = provider.getAccessibilityRepository(
                    application);
            final VibratorRepository vibrator = provider.getVibratorRepository(application);
            if (fingerprint != null && accessibility != null && vibrator != null) {
                return (T) new FingerprintEnrollEnrollingViewModel(application, fingerprint,
                        accessibility, vibrator);
            }
        }
        return create(modelClass);
    }
}
