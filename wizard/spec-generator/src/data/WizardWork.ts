/** Exclusive workspace work that must block navigation and page unload. */
export type WizardWork = "uploading" | "validating" | "packaging" | null;
