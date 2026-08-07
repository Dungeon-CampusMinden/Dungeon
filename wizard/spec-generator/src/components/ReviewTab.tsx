import React from "react";
import { toast } from "sonner";
import { DownloadIcon } from "lucide-react";
import type { DeerSchema } from "@/data/DeerSchema";
import { ErrorChecker, type IssueReport } from "@/data/ErrorChecker";
import { createSchemaZip, downloadBlob } from "@/data/AdventurePackage";
import { Alert, AlertDescription, AlertTitle } from "./ui/alert";
import { Button } from "./ui/button";
import { IssueList } from "./IssueList";

export function ReviewTab({ deerSchema, issueReport }: { deerSchema: DeerSchema; issueReport: IssueReport }) {
  const [generating, setGenerating] = React.useState(false);

  const issues = ErrorChecker.getSortedIssues(issueReport);
  const blockingIssues = issues.filter((issue) => issue.severity !== "info");
  const errorCount = blockingIssues.filter((issue) => issue.severity === "error").length;
  const warningCount = blockingIssues.length - errorCount;

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

      <code className="rounded-sm bg-slate-100 p-4 text-sm text-slate-900">
        <pre className="max-h-96 overflow-y-auto whitespace-pre-wrap break-all mt-0 p-0">
          {JSON.stringify(deerSchema, null, 2)}
        </pre>
      </code>

      <Button
        className="self-stretch"
        size="lg"
        onClick={generate}
        disabled={blockingIssues.length > 0 || generating}
      >
        <DownloadIcon />
        {generating ? "Wird generiert…" : "Generieren"}
      </Button>
    </div>
  );
}
