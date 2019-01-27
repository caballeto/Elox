package com.lang.elox.utils;

import com.lang.elox.errors.CircularImportError;

import java.util.ArrayList;
import java.util.List;

// TODO: 1/25/19 Fix: non thread-safe singleton
public final class ImportValidator {
  private final ArrayList<String> files = new ArrayList<>();

  public List<String> path = new ArrayList<>();

  public void setFile(String file) {
    files.add(file);
  }

  public void add(String file) {
    files.add(file);
  }

  public String pop() {
    if (files.isEmpty()) return null;
    return files.remove(files.size() - 1);
  }

  public void clear() {
    files.clear();
  }

  public String prefix() {
    return String.join("", path);
  }

  public void validateFile(String filename) {
    for (int i = 0; i < files.size(); i++) {
      if (files.get(i).equals(filename)) {
        List<String> list = files.subList(i, files.size());
        list.add(filename);
        throw new CircularImportError(list);
      }
    }
  }
}
