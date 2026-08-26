import React from "react";
import type { WizardDraft } from "@/data/WizardDraft";

interface UploadReferencesValue {
  draftId: string;
  uploads: WizardDraft["uploads"];
}

const UploadReferencesContext = React.createContext<UploadReferencesValue>({ draftId: "", uploads: {} });

export function UploadReferencesProvider({ draftId, value, children }: {
  draftId: string;
  value: WizardDraft["uploads"];
  children: React.ReactNode;
}) {
  return <UploadReferencesContext.Provider value={{ draftId, uploads: value }}>{children}</UploadReferencesContext.Provider>;
}

export function useUploadReferences(): WizardDraft["uploads"] {
  return React.useContext(UploadReferencesContext).uploads;
}

export function usePreviewDraftId(): string {
  return React.useContext(UploadReferencesContext).draftId;
}
