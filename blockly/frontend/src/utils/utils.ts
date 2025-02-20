/**
 * Utility function to pause execution for a specified number of seconds.
 *
 * @param seconds - The number of seconds to sleep. Must be a non-negative number.
 * @returns A promise that resolves after the specified number of seconds.
 * @throws Error Will throw an error if the sleep duration is negative.
 */
export const sleep = (seconds: number) => {
    if (seconds < 0) {
        throw new Error("Sleep duration cannot be negative");
    }
    return new Promise((resolve) => setTimeout(resolve, seconds * 1000));
}
