import type { DeerSchema } from "@/data/DeerSchema";

export function ErrorDetector({
  deerSchema,
  updateDeerSchema,
  className,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
  className?: string;
}) {
  return (
    <div className={`panel rounded-sm ${className ?? ""}`}>
      <h2>Fehlerübersicht</h2>
      <p className="text-muted-foreground">
        Sobal alle Felder ausgefüllt sind, wird hier eine Übersicht aller gefundenen Fehler angezeigt.
      </p>
    </div>
  );
}
