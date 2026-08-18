import React from "react";
import type { WizardDraft } from "@/data/WizardDraft";

const UploadReferencesContext = React.createContext<WizardDraft["uploads"]>({});

export const UploadReferencesProvider = UploadReferencesContext.Provider;

export function useUploadReferences(): WizardDraft["uploads"] {
  return React.useContext(UploadReferencesContext);
}
