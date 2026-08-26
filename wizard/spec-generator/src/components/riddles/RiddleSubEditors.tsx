import type {
  AnyResource,
  Asset,
  DeerProject,
  HintSeverity,
  InformationSource,
  Riddle,
  RiddleHint,
} from "@/data/DeerSchema";
import { Util } from "@/data/Util";
import { PlusIcon, TrashIcon } from "lucide-react";
import { AssetSelector } from "../assets/AssetSelector";
import { addInformationSource, removeInformationSource } from "@/data/RiddleGraphActions";
import { Button } from "../ui/button";
import { Field, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { Textarea } from "../ui/textarea";
import { HINT_SEVERITIES, RESOURCE_KINDS } from "./riddleTypes";
import { ResponsiveChoice } from "../ui/responsive-choice";

export function InformationSourceListEditor({
  project,
  riddle,
  onChange,
}: {
  project: DeerProject;
  riddle: Riddle;
  onChange: (updated: DeerProject) => void;
}) {
  const informationSources = riddle.informationSources;
  const updateProject = (transform: (next: DeerProject, nextRiddle: Riddle) => void) => {
    const next = structuredClone(project);
    const nextRiddle = next.riddles.find((candidate) => candidate.id === riddle.id);
    if (!nextRiddle) return;
    transform(next, nextRiddle);
    onChange(next);
  };

  const updateSource = (index: number, updated: InformationSource) => {
    updateProject((_next, nextRiddle) => {
      nextRiddle.informationSources[index] = updated;
    });
  };

  const removeSource = (index: number) => {
    const sourceId = informationSources[index]?.id;
    if (sourceId) updateProject((next, nextRiddle) => removeInformationSource(next, nextRiddle, sourceId));
  };

  return (
    <div className="flex flex-col gap-3">
      <Button
        onClick={() => updateProject((next, nextRiddle) => addInformationSource(next, nextRiddle))}
        className="lg:max-w-40"
      >
        <PlusIcon />
        Informationsquelle
      </Button>
      {informationSources.length === 0 && (
        <span className="text-sm text-muted-foreground">Noch keine Informationsquelle hinterlegt.</span>
      )}
      {informationSources.map((source, index) => (
        <div
          key={source.id}
          className="flex flex-col gap-3 rounded-md border border-border p-3"
        >
          <div className="flex items-center justify-between gap-2">
            <span className="text-sm font-medium">Informationsquelle {index + 1}</span>
            <Button aria-label={`Informationsquelle ${index + 1} löschen`} variant="destructive" size="icon" onClick={() => removeSource(index)}>
              <TrashIcon />
            </Button>
          </div>
          <ResourceListEditor
            resources={source.resources}
            assets={project.assets}
            onChange={(updated) => updateSource(index, { ...source, resources: updated })}
          />
        </div>
      ))}
    </div>
  );
}

export function ResourceListEditor({
  resources,
  onChange,
  assets,
}: {
  resources: AnyResource[];
  onChange: (updated: AnyResource[]) => void;
  assets: Asset[];
}) {
  const updateResource = (index: number, updated: AnyResource) => {
    const next = [...resources];
    next[index] = updated;
    onChange(next);
  };

  const addResource = () => {
    const newResource: AnyResource = {
      id: Util.generateUniqueId("res"),
      kind: "inline_text",
      title: "Neues Material",
      text: "",
    };
    onChange([...resources, newResource]);
  };

  const removeResource = (index: number) => {
    const next = [...resources];
    next.splice(index, 1);
    onChange(next);
  };

  const changeKind = (index: number, kind: string) => {
    const resource = resources[index];
    if (resource.kind === kind) return;
    if (kind === "asset") {
      updateResource(index, {
        id: resource.id,
        kind: "asset",
        title: resource.title,
        assetId: "",
      });
    } else {
      updateResource(index, {
        id: resource.id,
        kind: "inline_text",
        title: resource.title,
        text: "",
      });
    }
  };

  return (
    <div className="flex flex-col gap-3">
      <Button onClick={addResource} className="lg:max-w-40">
        <PlusIcon />
        Material
      </Button>
      {resources.length === 0 && (
        <span className="text-sm text-muted-foreground">Noch kein Material hinterlegt.</span>
      )}
      <div className="grid grid-cols-1 items-start gap-3 sm:grid-cols-2 lg:grid-cols-2">
        {resources.map((resource, index) => (
          <div
            key={resource.id}
            className="flex flex-col gap-3 rounded-md border border-border p-3"
          >
            <div className="grid grid-cols-[1fr_auto] items-end gap-2">
              <Field>
                <FieldLabel>Titel</FieldLabel>
                <Input
                  aria-label={`Titel von Material ${index + 1}`}
                  value={resource.title}
                  onChange={(e) => updateResource(index, { ...resource, title: e.target.value })}
                />
              </Field>
              <Button aria-label={`Material ${index + 1} löschen`} variant="destructive" size="icon" onClick={() => removeResource(index)}>
                <TrashIcon />
              </Button>
            </div>
            <Field>
              <FieldLabel>Art</FieldLabel>
              <ResponsiveChoice
                accessibleLabel={`Art von Material ${index + 1}`}
                options={RESOURCE_KINDS}
                value={resource.kind}
                onChange={(newValue) => changeKind(index, newValue)}
              />
            </Field>
            {resource.kind === "inline_text" ? (
              <Field>
                <FieldLabel>Text</FieldLabel>
                <Textarea
                  aria-label={`Text von Material ${index + 1}`}
                  value={resource.text}
                  onChange={(e) => updateResource(index, { ...resource, text: e.target.value })}
                />
              </Field>
            ) : (
              <Field>
                <FieldLabel>Datei</FieldLabel>
                <AssetSelector
                  accessibleLabel={`Datei für Material ${index + 1}`}
                  items={assets}
                  value={resource.assetId}
                  onChange={(newValue) => updateResource(index, { ...resource, assetId: newValue })}
                />
              </Field>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export function HintListEditor({
  hints,
  onChange,
}: {
  hints: RiddleHint[];
  onChange: (updated: RiddleHint[]) => void;
}) {
  const updateHint = (index: number, updated: RiddleHint) => {
    const next = [...hints];
    next[index] = updated;
    onChange(next);
  };

  const addHint = () => {
    onChange([
      ...hints,
      { id: Util.generateUniqueId("h"), title: "Neue Hilfe", text: "", severity: "orientation" },
    ]);
  };

  const removeHint = (index: number) => {
    const next = [...hints];
    next.splice(index, 1);
    onChange(next);
  };

  return (
    <div className="flex flex-col gap-3">
      <Button onClick={addHint} className="lg:max-w-40">
        <PlusIcon />
        Hilfe
      </Button>
      {hints.length === 0 && (
        <span className="text-sm text-muted-foreground">Noch keine Hilfe hinterlegt.</span>
      )}
      <div className="grid grid-cols-1 items-start gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {hints.map((hint, index) => (
          <div
            key={hint.id}
            className="flex flex-col gap-3 rounded-md border border-border p-3"
          >
            <div className="grid grid-cols-[1fr_auto] items-end gap-2">
              <Field>
                <FieldLabel>Titel</FieldLabel>
                <Input
                  aria-label={`Titel von Hilfe ${index + 1}`}
                  value={hint.title}
                  onChange={(e) => updateHint(index, { ...hint, title: e.target.value })}
                />
              </Field>
              <Button aria-label={`Hilfe ${index + 1} löschen`} variant="destructive" size="icon" onClick={() => removeHint(index)}>
                <TrashIcon />
              </Button>
            </div>
            <Field>
              <FieldLabel>Text</FieldLabel>
              <Textarea
                aria-label={`Text von Hilfe ${index + 1}`}
                value={hint.text}
                onChange={(e) => updateHint(index, { ...hint, text: e.target.value })}
              />
            </Field>
            <Field>
              <FieldLabel>Stufe</FieldLabel>
              <ResponsiveChoice
                accessibleLabel={`Stufe von Hilfe ${index + 1}`}
                options={HINT_SEVERITIES}
                value={hint.severity}
                onChange={(newValue) => updateHint(index, { ...hint, severity: newValue as HintSeverity })}
              />
            </Field>
          </div>
        ))}
      </div>
    </div>
  );
}
