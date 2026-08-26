/** Exclusive work that blocks page unload and competing work; only uploads block step navigation. */
export type WizardWork = "uploading" | "validating" | "packaging" | null;
