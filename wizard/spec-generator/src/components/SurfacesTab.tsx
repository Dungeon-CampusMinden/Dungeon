import type { DeerSchema, Surface } from "@/data/DeerSchema";
import { Input } from "./ui/input";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { CircleQuestionMarkIcon, PlusIcon, SquareDashedIcon, TrashIcon } from "lucide-react";
import { CustomIcon } from "./CustomIcon";
import { Button } from "./ui/button";
import { Util } from "@/data/Util";
import React from "react";
import { Separator } from "./ui/separator";

const SURFACE_TYPES = [
  { value: "world", icon: <SquareDashedIcon />, description: "Ein Raum in der Welt", label: "Raum" },
  { value: "container", icon: "container", description: "Ein Container in einem Raum", label: "Container" },
  { value: "keypad", icon: "keypad", description: "Ein Keypad, das eine Eingabe erfordert", label: "Keypad" },
  { value: "door", icon: "door", description: "Eine Tür, die den Zugang zu etwas versperrt", label: "Tür" },
];

export function SurfacesTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  const surfaces = deerSchema.surfaces;
  const [testSelectedId, setTestSelectedId] = React.useState<string | null>(null);

  const addSurface = () => {
    const newSurface: Surface = {
      id: Util.generateUniqueId("s"),
      title: "Neuer Ort",
      kind: "world",
    };
    surfaces.push(newSurface);
    updateDeerSchema(deerSchema);
  };

  const deleteSurface = (index: number) => {
    surfaces.splice(index, 1);
    updateDeerSchema(deerSchema);
  };

  return (
    <div className="flex flex-col gap-0">
      <h1>Orte & Geräte</h1>
      <p className="text-sm text-muted-foreground">
        Hier kannst du die Orte und Geräte definieren, die in deinem Abenteuer vorkommen.
      </p>
      <Button onClick={addSurface} className="my-2 max-w-40">
        <PlusIcon />
        Hinzufügen
      </Button>
      <div className="flex flex-row flex-wrap gap-4">
        {surfaces.map((surface, index) => (
          <SurfaceEntry
            key={index}
            surface={surface}
            setSurface={(updatedSurface) => {
              surfaces[index] = updatedSurface;
              updateDeerSchema(deerSchema);
            }}
            onDelete={() => deleteSurface(index)}
          />
        ))}
      </div>
      <Separator className="my-4" />
      <h3>Testauswahl</h3>
      <SurfaceSelector
        items={surfaces}
        value={testSelectedId ?? ""}
        onChange={(newValue) => {
          setTestSelectedId(newValue);
        }}
      />
    </div>
  );
}

export function SurfaceEntry({
  surface,
  setSurface,
  onDelete,
}: {
  surface: Surface;
  setSurface: (updatedSurface: Surface) => void;
  onDelete: () => void;
}) {
  return (
    <div className="flex flex-col gap-1 p-2 border border-[var(--border-color)] rounded-md">
      <div className="flex flex-row gap-1 items-center">
        <Input
          value={surface.title}
          onChange={(e) => {
            surface.title = e.target.value;
            setSurface(surface);
          }}
          className="max-w-[200px]"
        />
        <Button variant="destructive" size="icon" className="" onClick={onDelete}>
          <TrashIcon />
        </Button>
      </div>
      <Select
        items={SURFACE_TYPES}
        value={surface.kind}
        onValueChange={(newValue) => {
          surface.kind = newValue ?? "world";
          setSurface(surface);
        }}
      >
        <SelectTrigger className="">
          <SelectValue
            placeholder="Wähle einen Ort"
            render={(props, selectValue) => {
              const surface =
                SURFACE_TYPES.find((type) => type.value === selectValue.value) ?? SURFACE_TYPES[0];
              return (
                <div className="flex items-center gap-2" {...props}>
                  <SurfaceIcon kind={selectValue.value} size={20} />
                  <span>{surface.label}</span>
                </div>
              );
            }}
          />
        </SelectTrigger>
        <SelectContent alignItemWithTrigger={false} className="min-w-[max-content]">
          <SelectGroup>
            {SURFACE_TYPES.map((item) => (
              <SelectItem key={item.value} value={item.value}>
                <div className="flex flex-col gap-1">
                  <div className="flex items-center gap-2">
                    <div className="flex items-center justify-center w-6">
                      <SurfaceIcon kind={item.value} size={20} />
                    </div>
                    {item.label}
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-6">&nbsp;</div>
                    <span className="text-xs text-muted-foreground">{item.description}</span>
                  </div>
                </div>
              </SelectItem>
            ))}
          </SelectGroup>
        </SelectContent>
      </Select>
    </div>
  );
}

export function SurfaceIcon({ kind, size = 16 }: { kind: string; size?: number }) {
  const surfaceType = SURFACE_TYPES.find((type) => type.value === kind);
  if (!surfaceType) {
    return <CircleQuestionMarkIcon />;
  }
  if (typeof surfaceType.icon === "string") {
    const path = `/icons/surfaces/${surfaceType.icon}.png`;
    return <CustomIcon src={path} alt={surfaceType.description} size={size} />;
  }
  return surfaceType.icon;
}

export function SurfaceSelector({
  items,
  value,
  onChange,
}: {
  items: Surface[];
  value: string;
  onChange: (newValue: string) => void;
}) {
  // Sort by kind first then by title
  const itemsSorted = [...items].sort((a, b) => {
    if (a.kind === b.kind) {
      return a.title.localeCompare(b.title);
    }
    return a.kind.localeCompare(b.kind);
  });
  return (
    <Select
      items={SURFACE_TYPES.map((type) => ({
        label: type.label,
        value: type.value,
      }))}
      value={value}
      onValueChange={(newValue) => {
        onChange(newValue ?? "");
      }}
    >
      <SelectTrigger className="w-full min-w-0 max-w-full">
        <SelectValue
          placeholder="Wähle einen Ort"
          render={(props, selectValue) => {
            const surface = items.find((item) => item.id === selectValue.value);
            if (!surface) {
              return (
                <span {...props} className="min-w-0 flex-1 truncate">
                  Unbekannter Ort
                </span>
              );
            }
            return (
              <div {...props} className="flex min-w-0 flex-1 items-center gap-2">
                <SurfaceIcon kind={surface.kind} size={20} />
                <span className="min-w-0 flex-1 truncate">{surface.title}</span>
              </div>
            );
          }}
        />
      </SelectTrigger>
      <SelectContent alignItemWithTrigger={false} className="min-w-[max-content]">
        <SelectGroup>
          <SelectItem value="">
            <div className="flex items-center gap-2">
              <div className="flex items-center justify-center w-6">
                <CircleQuestionMarkIcon size={20} />
              </div>
              <span>Kein Ort ausgewählt</span>
            </div>
          </SelectItem>
          {itemsSorted.map((item) => (
            <SelectItem key={item.id} value={item.id}>
              <div className="flex items-center gap-2">
                <div className="flex items-center justify-center w-6">
                  <SurfaceIcon kind={item.kind} size={20} />
                </div>
                {item.title}
              </div>
            </SelectItem>
          ))}
        </SelectGroup>
      </SelectContent>
    </Select>
  );
}
