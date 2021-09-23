// Generated by Dagger (https://dagger.dev).
package com.benatt.passwordsmanager.utils;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import com.benatt.passwordsmanager.data.models.user.UserRepository;
import dagger.internal.Factory;
import javax.crypto.SecretKey;
import javax.inject.Provider;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class ViewModelFactory_Factory implements Factory<ViewModelFactory> {
  private final Provider<PasswordRepository> passwordRepositoryProvider;

  private final Provider<UserRepository> userRepositoryProvider;

  private final Provider<SecretKey> secretKeyProvider;

  public ViewModelFactory_Factory(Provider<PasswordRepository> passwordRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<SecretKey> secretKeyProvider) {
    this.passwordRepositoryProvider = passwordRepositoryProvider;
    this.userRepositoryProvider = userRepositoryProvider;
    this.secretKeyProvider = secretKeyProvider;
  }

  @Override
  public ViewModelFactory get() {
    return newInstance(passwordRepositoryProvider.get(), userRepositoryProvider.get(), secretKeyProvider.get());
  }

  public static ViewModelFactory_Factory create(
      Provider<PasswordRepository> passwordRepositoryProvider,
      Provider<UserRepository> userRepositoryProvider, Provider<SecretKey> secretKeyProvider) {
    return new ViewModelFactory_Factory(passwordRepositoryProvider, userRepositoryProvider, secretKeyProvider);
  }

  public static ViewModelFactory newInstance(PasswordRepository passwordRepository,
      UserRepository userRepository, SecretKey secretKey) {
    return new ViewModelFactory(passwordRepository, userRepository, secretKey);
  }
}
