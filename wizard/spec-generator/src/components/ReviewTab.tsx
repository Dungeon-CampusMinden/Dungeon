import React from "react";
import { toast } from "sonner";
import { DownloadIcon, DicesIcon } from "lucide-react";
import type { DeerSchema } from "@/data/DeerSchema";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import { createSchemaZip, downloadBlob } from "@/data/AdventurePackage";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { Button } from "./ui/button";
import { Field, FieldDescription, FieldError, FieldLabel } from "./ui/field";
import { IssueList } from "./IssueList";
import { Input } from "./ui/input";
import { Util } from "@/data/Util";

const MAX_SEED = BigInt(Number.MAX_SAFE_INTEGER);

function parseSeed(value: string): number | null {
  if (!/^\d+$/.test(value)) return null;

  const parsed = BigInt(value);
  if (parsed > MAX_SEED) return null;

  return Number(parsed);
}

export function ReviewTab({
  deerSchema,
  updateDeerSchema,
  issueReport,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
  issueReport: IssueReport;
}) {
  const [generating, setGenerating] = React.useState(false);
  const [seedInput, setSeedInput] = React.useState(String(deerSchema.seed ?? ""));

  const issues = ErrorChecker.getSortedIssues(issueReport);
  const errorCount = issues.filter((issue) => issue.severity === "error").length;
  const warningCount = issues.filter((issue) => issue.severity === "warning").length;
  const seed = parseSeed(seedInput);
  const seedError =
    seed === null
      ? `Der Seed muss eine Ganzzahl zwischen 0 und ${Number.MAX_SAFE_INTEGER} sein.`
      : undefined;

  React.useEffect(() => {
    setSeedInput(String(deerSchema.seed ?? ""));
  }, [deerSchema.seed]);

  const updateSeed = (value: string) => {
    setSeedInput(value);
    const newSeed = parseSeed(value);
    if (newSeed === null) return;

    updateDeerSchema({ ...deerSchema, seed: newSeed });
  };

  const randomizeSeed = () => {
    const newSeed = Util.generateSafeInteger();
    setSeedInput(String(newSeed));
    updateDeerSchema({ ...deerSchema, seed: newSeed });
  };

  const generateAdventure = async () => {
    setGenerating(true);
    try {
      const blob = await createSchemaZip(deerSchema);
      downloadBlob(blob, adventureFileName(deerSchema.metadata.title));
      toast.success("Das Abenteuer wurde generiert.");
    } catch (error) {
      toast.error("Das Abenteuer konnte nicht generiert werden.", {
        description: error instanceof Error ? error.message : undefined,
      });
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="flex flex-col gap-3">
      <h1>Prüfen & Generieren</h1>

      {errorCount > 0 && (
        <Alert variant="destructive" className="mt-0">
          <AlertTitle>Das Abenteuer ist noch nicht vollständig.</AlertTitle>
          <AlertDescription>
            {errorCount} {errorCount === 1 ? "Fehler muss" : "Fehler müssen"} behoben werden, bevor
            das Abenteuer generiert werden kann.
          </AlertDescription>
        </Alert>
      )}
      {warningCount > 0 && (
        <Alert className="mt-0 border-yellow-500/40 text-yellow-500">
          <AlertTitle>
            {warningCount} {warningCount === 1 ? "Warnung" : "Warnungen"}
          </AlertTitle>
          <AlertDescription>Warnungen verhindern die Generierung des Abenteuers nicht.</AlertDescription>
        </Alert>
      )}
      {errorCount === 0 && warningCount === 0 && (
        <IssueList className="mt-0" issues={[]} emptyMessage="Das Abenteuer kann generiert werden." />
      )}
      {issues.length > 0 && <IssueList className="mt-0" issues={issues} />}

      <Field>
        <FieldLabel htmlFor="seed">Seed</FieldLabel>
        <div className="flex gap-2">
          <Input
            id="seed"
            type="text"
            inputMode="numeric"
            value={seedInput}
            onChange={(event) => updateSeed(event.target.value)}
            aria-invalid={seedError !== undefined}
          />
          <Button type="button" variant="outline" onClick={randomizeSeed}>
            <DicesIcon />
            Zufälliger Seed
          </Button>
        </div>
        <FieldDescription>
          Der Seed bestimmt verschiedene zufällige Faktoren im Spiel, wie Anordnung von Gegenständen,
          Dekoration usw.
        </FieldDescription>
        {seedError && <FieldError>{seedError}</FieldError>}
      </Field>

      <code className="rounded-sm bg-slate-100 p-4 text-sm text-slate-900">
        <pre className="max-h-96 overflow-y-auto whitespace-pre-wrap break-all mt-0 p-0">
          {JSON.stringify(deerSchema, null, 2)}
        </pre>
      </code>

      <Button
        className="self-stretch"
        size="lg"
        onClick={generateAdventure}
        disabled={errorCount > 0 || seedError !== undefined || generating}
      >
        <DownloadIcon />
        {generating ? "Abenteuer wird generiert…" : "Abenteuer generieren"}
      </Button>
    </div>
  );
}

function adventureFileName(title: string): string {
  const safeTitle = title
    .trim()
    .toLowerCase()
    .replaceAll("ß", "ss")
    .normalize("NFKD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, 80)
    .replace(/-+$/g, "");
  const stem = safeTitle || "abenteuer";
  const safeStem = /^(con|prn|aux|nul|com[1-9]|lpt[1-9])$/.test(stem)
    ? `abenteuer-${stem}`
    : stem;
  return `${safeStem}.zip`;
}
