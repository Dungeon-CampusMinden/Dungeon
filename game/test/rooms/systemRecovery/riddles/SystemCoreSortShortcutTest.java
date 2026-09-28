package rooms.systemRecovery.riddles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SystemCoreSortShortcutTest {

  @Test
  void recognizesShortAndExplicitSortedArrayDeclarations() {
    assertTrue(SystemCoreSortShortcut.isDirectSortedArrayInitialization(
        "int[] array = {4, 8, 15, 16, 23, 42};"));
    assertTrue(SystemCoreSortShortcut.isDirectSortedArrayInitialization(
        "int array[] = new int[]{4,8,15,16,23,42};"));
  }

  @Test
  void commentsAndUnrelatedOrIncompleteArraysDoNotMatch() {
    assertFalse(SystemCoreSortShortcut.isDirectSortedArrayInitialization(
        "// int[] array = {4, 8, 15, 16, 23, 42};"));
    assertFalse(SystemCoreSortShortcut.isDirectSortedArrayInitialization(
        "int[] array = {4, 8, 15, 16, 23};"));
    assertFalse(SystemCoreSortShortcut.isDirectSortedArrayInitialization(
        "int[] other = {4, 8, 15, 16, 23, 42};"));
    assertFalse(SystemCoreSortShortcut.isDirectSortedArrayInitialization(
        "int[] array = {4, 8, 15, 16, 23, 24};"));
    assertFalse(SystemCoreSortShortcut.isDirectSortedArrayInitialization(
        "int[] array = {4, 8, 15, 16, 23, 42};\nint count = 6;"));
  }
}
