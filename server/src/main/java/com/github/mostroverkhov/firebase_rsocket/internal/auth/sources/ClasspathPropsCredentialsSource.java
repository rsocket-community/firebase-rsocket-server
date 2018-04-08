package com.github.mostroverkhov.firebase_rsocket.internal.auth.sources;

import com.github.mostroverkhov.firebase_rsocket.internal.auth.CredentialsSource;

/** Created with IntelliJ IDEA. Author: mostroverkhov */
public class ClasspathPropsCredentialsSource extends CredentialsSource {
  public ClasspathPropsCredentialsSource(String fileName) {
    super(new ClasspathCredentialsSupplier(fileName), new PropCredentialsFactory());
  }
}
