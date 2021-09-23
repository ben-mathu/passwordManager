// Generated by Dagger (https://dagger.dev).
package com.benatt.passwordsmanager.views.passwords;

import com.benatt.passwordsmanager.data.models.passwords.PasswordRepository;
import dagger.internal.Factory;
import javax.crypto.SecretKey;
import javax.inject.Provider;

@SuppressWarnings({
    "unchecked",
    "rawtypes"
})
public final class PasswordsViewModel_Factory implements Factory<PasswordsViewModel> {
  private final Provider<SecretKey> secretKeyProvider;

  private final Provider<PasswordRepository> passwordRepositoryProvider;

  public PasswordsViewModel_Factory(Provider<SecretKey> secretKeyProvider,
      Provider<PasswordRepository> passwordRepositoryProvider) {
    this.secretKeyProvider = secretKeyProvider;
    this.passwordRepositoryProvider = passwordRepositoryProvider;
  }

  @Override
  public PasswordsViewModel get() {
    return newInstance(secretKeyProvider.get(), passwordRepositoryProvider.get());
  }

  public static PasswordsViewModel_Factory create(Provider<SecretKey> secretKeyProvider,
      Provider<PasswordRepository> passwordRepositoryProvider) {
    return new PasswordsViewModel_Factory(secretKeyProvider, passwordRepositoryProvider);
  }

  public static PasswordsViewModel newInstance(SecretKey secretKey,
      PasswordRepository passwordRepository) {
    return new PasswordsViewModel(secretKey, passwordRepository);
  }
}
