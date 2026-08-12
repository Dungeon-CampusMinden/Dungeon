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

const MAX_SEED = 9223372036854775807n;

function parseSeed(value: string): number | null {
  if (!/^\d+$/.test(value)) return null;

  const parsed = BigInt(value);
  if (parsed > MAX_SEED) return null;

  return Number(parsed);
}

function generateRandomSeed(): number {
  return Math.floor(Math.random() * Number.MAX_SAFE_INTEGER);
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
  const blockingIssues = issues.filter((issue) => issue.severity !== "info");
  const errorCount = blockingIssues.filter((issue) => issue.severity === "error").length;
  const warningCount = blockingIssues.length - errorCount;
  const seed = parseSeed(seedInput);
  const seedError =
    seed === null ? "Der Seed muss eine nicht-negative Ganzzahl bis 9223372036854775807 sein." : undefined;

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
    const newSeed = generateRandomSeed();
    setSeedInput(String(newSeed));
    updateDeerSchema({ ...deerSchema, seed: newSeed });
  };

  const generate = async () => {
    setGenerating(true);
    try {
      const blob = await createSchemaZip(deerSchema);
      downloadBlob(blob, `${deerSchema.metadata.id || "deer"}.zip`);
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

      {blockingIssues.length > 0 ? (
        <Alert variant="destructive" className="mt-0">
          <AlertTitle>Das Abenteuer ist noch nicht vollständig.</AlertTitle>
          <AlertDescription>
            {errorCount} Fehler und {warningCount} Warnung(en) müssen behoben werden, bevor generiert werden
            kann.
          </AlertDescription>
        </Alert>
      ) : (
        <IssueList className="mt-0" issues={[]} emptyMessage="Das Abenteuer ist bereit zum Generieren." />
      )}

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
        onClick={generate}
        disabled={blockingIssues.length > 0 || seedError !== undefined || generating}
      >
        <DownloadIcon />
        {generating ? "Wird generiert…" : "Generieren"}
      </Button>
    </div>
  );
}
