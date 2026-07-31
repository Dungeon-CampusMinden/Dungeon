import type { AnyResource, Asset, RiddleHint } from "@/data/DeerSchema";
import { Util } from "@/data/Util";
import { PlusIcon, TrashIcon } from "lucide-react";
import { AssetSelector } from "../assets/AssetSelector";
import { Button } from "../ui/button";
import { Field, FieldLabel } from "../ui/field";
import { Input } from "../ui/input";
import { Slider } from "../ui/slider";
import { Textarea } from "../ui/textarea";
import { RESOURCE_AVAILABILITIES, RESOURCE_KINDS, RESOURCE_PURPOSES } from "./riddleTypes";
import { SimpleSelect } from "./SimpleSelect";

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
      availability: "inside_container",
      purpose: "clue",
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
        availability: resource.availability,
        purpose: resource.purpose,
        assetId: "",
      });
    } else {
      updateResource(index, {
        id: resource.id,
        kind: "inline_text",
        title: resource.title,
        availability: resource.availability,
        purpose: resource.purpose,
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
      {resources.map((resource, index) => (
        <div
          key={resource.id}
          className="flex flex-col gap-3 rounded-md border border-[var(--border-color)] p-3"
        >
          <div className="grid grid-cols-[1fr_auto] items-end gap-2">
            <Field>
              <FieldLabel>Titel</FieldLabel>
              <Input
                value={resource.title}
                onChange={(e) => updateResource(index, { ...resource, title: e.target.value })}
              />
            </Field>
            <Button variant="destructive" size="icon" onClick={() => removeResource(index)}>
              <TrashIcon />
            </Button>
          </div>
          <div className="grid grid-cols-1 gap-2 sm:grid-cols-3">
            <Field>
              <FieldLabel>Art</FieldLabel>
              <SimpleSelect
                options={RESOURCE_KINDS}
                value={resource.kind}
                onChange={(newValue) => changeKind(index, newValue)}
              />
            </Field>
            <Field>
              <FieldLabel>Verfügbarkeit</FieldLabel>
              <SimpleSelect
                options={RESOURCE_AVAILABILITIES}
                value={resource.availability}
                onChange={(newValue) =>
                  updateResource(index, {
                    ...resource,
                    availability: newValue as AnyResource["availability"],
                  })
                }
              />
            </Field>
            <Field>
              <FieldLabel>Zweck</FieldLabel>
              <SimpleSelect
                options={RESOURCE_PURPOSES}
                value={resource.purpose}
                onChange={(newValue) =>
                  updateResource(index, { ...resource, purpose: newValue as AnyResource["purpose"] })
                }
              />
            </Field>
          </div>
          {resource.kind === "inline_text" ? (
            <Field>
              <FieldLabel>Text</FieldLabel>
              <Textarea
                value={resource.text}
                onChange={(e) => updateResource(index, { ...resource, text: e.target.value })}
              />
            </Field>
          ) : (
            <Field>
              <FieldLabel>Datei</FieldLabel>
              <AssetSelector
                items={assets}
                value={resource.assetId}
                onChange={(newValue) => updateResource(index, { ...resource, assetId: newValue })}
              />
            </Field>
          )}
        </div>
      ))}
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
    onChange([...hints, { id: Util.generateUniqueId("h"), title: "Neue Hilfe", text: "", severity: 1 }]);
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
      {hints.map((hint, index) => (
        <div key={hint.id} className="flex flex-col gap-3 rounded-md border border-[var(--border-color)] p-3">
          <div className="grid grid-cols-[1fr_auto] items-end gap-2">
            <Field>
              <FieldLabel>Titel</FieldLabel>
              <Input
                value={hint.title}
                onChange={(e) => updateHint(index, { ...hint, title: e.target.value })}
              />
            </Field>
            <Button variant="destructive" size="icon" onClick={() => removeHint(index)}>
              <TrashIcon />
            </Button>
          </div>
          <Field>
            <FieldLabel>Text</FieldLabel>
            <Textarea
              value={hint.text}
              onChange={(e) => updateHint(index, { ...hint, text: e.target.value })}
            />
          </Field>
          <Field>
            <FieldLabel>Stufe: {hint.severity}</FieldLabel>
            <Slider
              value={hint.severity}
              onValueChange={(value) => updateHint(index, { ...hint, severity: value as number })}
              min={1}
              max={5}
              step={1}
            />
          </Field>
        </div>
      ))}
    </div>
  );
}
