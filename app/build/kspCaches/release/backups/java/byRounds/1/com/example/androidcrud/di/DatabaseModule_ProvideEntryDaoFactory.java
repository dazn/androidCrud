package com.example.androidcrud.di;

import com.example.androidcrud.data.local.AppDatabase;
import com.example.androidcrud.data.local.EntryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class DatabaseModule_ProvideEntryDaoFactory implements Factory<EntryDao> {
  private final Provider<AppDatabase> databaseProvider;

  public DatabaseModule_ProvideEntryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public EntryDao get() {
    return provideEntryDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideEntryDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideEntryDaoFactory(databaseProvider);
  }

  public static EntryDao provideEntryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideEntryDao(database));
  }
}
