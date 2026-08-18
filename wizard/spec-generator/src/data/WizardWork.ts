/** Exclusive workspace work that must block navigation and page unload. */
export type WizardWork = "uploading" | "choosing" | "validating" | "finalizing" | "packaging" | null;
