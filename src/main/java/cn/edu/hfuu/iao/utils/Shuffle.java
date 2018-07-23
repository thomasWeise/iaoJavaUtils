package cn.edu.hfuu.iao.utils;

import java.util.Objects;
import java.util.Random;

/**
 * A class for shuffling objects
 *
 * @param <T>
 *          the element type
 */
public abstract class Shuffle<T> {

  /** the random number generator */
  final Random m_random;
  /** the data */
  final T m_data;
  /** the current index */
  int m_index;

  /**
   * create a shuffle of {@code n} objects
   *
   * @param random
   *          the random number generator
   * @param data
   *          the data
   */
  Shuffle(final Random random, final T data) {
    super();
    this.m_data = Objects.requireNonNull(data);
    this.m_random = Objects.requireNonNull(random);
  }

  /**
   * Get the next batch of {@code count} randomly chosen objects into
   * {@code dest}
   *
   * @param dest
   *          the destination array
   * @param count
   *          the count
   * @param destStart
   *          the start index in the destination
   */
  public abstract void get(final T dest, final int destStart,
      final int count);

  /**
   * Randomize a sub-sequence of an array or permutation of
   * {@code java.lang.Objects}. After this procedure, the {@code count}
   * elements of the array beginning at index {@code start} are uniformly
   * randomly distributed.
   *
   * @param array
   *          the array of {@code java.lang.Object}s whose sub-sequence to
   *          be randomized
   * @param start
   *          the start index
   * @param count
   *          the number of elements to be randomized
   * @param random
   *          the randomizer
   */
  public static final void shuffle(final Random random,
      final java.lang.Object[] array, final int start, final int count) {
    final int n;
    java.lang.Object t;
    int i, j, k;

    if (count > 0) {
      n = array.length;
      for (i = count; i > 1;) {
        j = ((start + random.nextInt(i--)) % n);
        k = ((start + i) % n);
        t = array[k];
        array[k] = array[j];
        array[j] = t;
      }
    }
  }

  /**
   * Randomize a sub-sequence of an array or permutation of
   * {@code java.lang.Objects}. After this procedure, the {@code count}
   * elements of the array beginning at index {@code start} are uniformly
   * randomly distributed.
   *
   * @param array
   *          the array of {@code java.lang.Object}s whose sub-sequence to
   *          be randomized
   * @param start
   *          the start index
   * @param count
   *          the number of elements to be randomized
   * @param random
   *          the randomizer
   */
  public static final void shuffle(final Random random, final int[] array,
      final int start, final int count) {
    final int n;
    int i, j, k, t;

    if (count > 0) {
      n = array.length;
      for (i = count; i > 1;) {
        j = ((start + random.nextInt(i--)) % n);
        k = ((start + i) % n);
        t = array[k];
        array[k] = array[j];
        array[j] = t;
      }
    }
  }

  /** shuffle integer values */
  public static final class ShuffleInts extends Shuffle<int[]> {
    /**
     * create a shuffle of a {@code data} array
     *
     * @param random
     *          the random number generator
     * @param data
     *          the data
     */
    public ShuffleInts(final Random random, final int[] data) {
      super(random, data);
      Shuffle.shuffle(this.m_random, this.m_data, 0, data.length);
    }

    /**
     * create a canonical permutation
     *
     * @param n
     *          the length
     * @return the permutation
     */
    private static final int[] __canonical(final int n) {
      final int[] res = new int[n];
      for (int i = n; (--i) >= 0;) {
        res[i] = i;
      }
      return (res);
    }

    /**
     * create a shuffle of {@code n} objects
     *
     * @param random
     *          the random number generator
     * @param n
     *          the number of objects
     */
    public ShuffleInts(final Random random, final int n) {
      this(random, ShuffleInts.__canonical(n));
    }

    /** {@inheritDoc} */
    @Override
    public final void get(final int[] dest, final int destStart,
        final int count) {

      if (count > 0) {
        int total = count;
        int start = destStart;
        final int[] data = this.m_data;
        final int size = data.length;
        int index = this.m_index;

        for (;;) {
          final int current = ((size > total) ? total : size);
          final int maxEnd = index + current;

          if (maxEnd <= size) {
            System.arraycopy(data, index, dest, start, current);
            index = maxEnd;
            break;
          }

          final int copy = size - index;
          if (copy > 0) {
            System.arraycopy(data, index, dest, start, copy);
          }
          index = 0;
          Shuffle.shuffle(this.m_random, data, 0, size);

          if ((total -= copy) <= 0) {
            break;
          }
          start += copy;
        }

        this.m_index = index;
      } else {
        if (count < 0) {
          throw new IllegalArgumentException(//
              "Illegal count: " + count); //$NON-NLS-1$
        }
      }
    }
  }
}
