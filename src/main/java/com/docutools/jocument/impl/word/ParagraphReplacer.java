package com.docutools.jocument.impl.word;

import java.util.List;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * Utility class for replacing placeholders in {@link XWPFParagraph}s.
 *
 * <p>This class handles placeholders that might be split across multiple {@link XWPFRun}s due to different formatting
 * or Word's internal XML structure. It consolidates such split runs before performing the replacement to ensure
 * that the placeholder is matched correctly while preserving the formatting of the first run of the placeholder.</p>
 */
public class ParagraphReplacer {
  /**
   * Replaces all occurrences of the given pattern in the paragraph using the provided resolver function.
   *
   * @param paragraph the paragraph to process
   * @param pattern   the regex pattern to match placeholders
   * @param resolver  the function to resolve a match result into a replacement string
   */
  public static void replaceText(XWPFParagraph paragraph,
                                 Pattern pattern,
                                 Function<MatchResult, String> resolver) {
    // First, find any matches that are split across multiple runs and merge them into a single run.
    consolidatePlaceholderRuns(paragraph, pattern);

    // Now that placeholders are consolidated into single runs, we can safely iterate and replace.
    for (XWPFRun run : paragraph.getRuns()) {
      String text = run.getText(0);
      if (text == null || text.isEmpty()) {
        continue;
      }

      Matcher m = pattern.matcher(text);
      if (!m.find()) {
        continue;
      }

      // We use replaceAll with a function to apply the resolver to each match found in the run.
      String replaced = m.replaceAll(mr -> Matcher.quoteReplacement(resolver.apply(mr)));
      setRunText(run, replaced);
    }
  }

  /**
   * Consolidates runs that contain parts of the same placeholder.
   *
   * <p>Word often splits text into multiple runs for various reasons (formatting, spellcheck, etc.).
   * This method ensures that any match of the pattern that spans across run boundaries is merged into the
   * first run of that match.</p>
   *
   * @param paragraph the paragraph to consolidate
   * @param pattern   the pattern to look for
   */
  private static void consolidatePlaceholderRuns(XWPFParagraph paragraph, Pattern pattern) {
    // Loop until no more cross-run placeholders are found, since each merge mutates the run list.
    // This re-scans the paragraph after each merge, which is safe for typical paragraphs.
    while (mergeOneCrossRunMatch(paragraph, pattern)) { /* keep going */ }
  }

  /**
   * Finds the first match that spans multiple runs and merges those runs.
   *
   * @param paragraph the paragraph to process
   * @param pattern   the pattern to match
   * @return true if a merge occurred, false otherwise
   */
  private static boolean mergeOneCrossRunMatch(XWPFParagraph paragraph, Pattern pattern) {
    List<XWPFRun> runs = paragraph.getRuns();
    StringBuilder all = new StringBuilder();
    int[] runEnds = new int[runs.size()];

    // Build a flat string representation of the paragraph and track run boundaries.
    for (int i = 0; i < runs.size(); i++) {
      String t = runs.get(i).getText(0);
      all.append(t == null ? "" : t);
      runEnds[i] = all.length();
    }

    Matcher m = pattern.matcher(all);
    while (m.find()) {
      int firstRun = runIndexFor(runEnds, m.start());
      int lastRun = runIndexFor(runEnds, m.end() - 1);

      // If the match is already contained within a single run, we skip it here.
      // It will be handled in the main replacement loop.
      if (firstRun == lastRun) {
        continue;
      }

      // Merge text of all runs from firstRun to lastRun into firstRun.
      // NOTE: The formatting of the firstRun wins (tie-breaking).
      StringBuilder merged = new StringBuilder();
      for (int i = firstRun; i <= lastRun; i++) {
        String t = runs.get(i).getText(0);
        if (t != null) {
          merged.append(t);
        }
      }
      setRunText(runs.get(firstRun), merged.toString());

      // Remove the now redundant runs from the paragraph.
      // We remove from the tail to the head to keep indices stable.
      for (int i = lastRun; i > firstRun; i--) {
        paragraph.removeRun(i);
      }
      return true;
    }
    return false;
  }

  /**
   * Finds the index of the run that contains the character at the given flat position.
   *
   * @param runEnds the array of cumulative run lengths
   * @param pos     the character position in the flat string
   * @return the index of the run
   */
  private static int runIndexFor(int[] runEnds, int pos) {
    for (int i = 0; i < runEnds.length; i++) {
      if (pos < runEnds[i]) {
        return i;
      }
    }
    return runEnds.length - 1;
  }

  /**
   * Sets the text of a run, preserving line breaks by adding {@code <w:br/>} tags.
   *
   * @param run     the run to update
   * @param newText the new text to set
   */
  private static void setRunText(XWPFRun run, String newText) {
    // We clear the existing text children to replace them with the new text and breaks.
    run.getCTR().setTArray(new CTText[] {});
    String[] lines = newText.split("(\\r\\n|\\r|\\n)");
    for (int i = 0; i < lines.length; i++) {
      if (i > 0) {
        run.addBreak();
      }
      run.setText(lines[i]);
    }
  }
}
