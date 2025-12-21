package com.example.androidcrud.data.repository;

import com.example.androidcrud.data.local.EntryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class EntryRepository_Factory implements Factory<EntryRepository> {
  private final Provider<EntryDao> entryDaoProvider;

  public EntryRepository_Factory(Provider<EntryDao> entryDaoProvider) {
    this.entryDaoProvider = entryDaoProvider;
  }

  @Override
  public EntryRepository get() {
    return newInstance(entryDaoProvider.get());
  }

  public static EntryRepository_Factory create(Provider<EntryDao> entryDaoProvider) {
    return new EntryRepository_Factory(entryDaoProvider);
  }

  public static EntryRepository newInstance(EntryDao entryDao) {
    return new EntryRepository(entryDao);
  }
}
