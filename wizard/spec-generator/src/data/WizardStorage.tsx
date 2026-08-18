import React from "react";
import type { AssetStoragePort } from "./AssetStorage";
import type { DraftStoragePort } from "./DraftStorage";

export interface WizardStoragePort {
  drafts: DraftStoragePort;
  assets: AssetStoragePort;
}

const WizardStorageContext = React.createContext<WizardStoragePort | null>(null);

export const WizardStorageProvider = WizardStorageContext.Provider;

export function useWizardStorage(): WizardStoragePort {
  const storage = React.useContext(WizardStorageContext);
  if (!storage) throw new Error("Wizard-Speicher wurde nicht bereitgestellt.");
  return storage;
}
