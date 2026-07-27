import type { DeerSchema } from "@/data/DeerSchema";
import { Field, FieldError, FieldGroup, FieldLabel, FieldSeparator, FieldSet } from "./ui/field";
import { Input } from "./ui/input";
import { StringListEditor } from "./StringListEditor";
import { Select, SelectContent, SelectGroup, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { Slider } from "./ui/slider";

export function RiddlesTab({
  deerSchema,
  updateDeerSchema,
}: {
  deerSchema: DeerSchema;
  updateDeerSchema: (updatedSchema: DeerSchema) => void;
}) {
  return (
    <div className="flex flex-col gap-0">
      <h1>Rätsel</h1>
      <FieldSet>
        <FieldGroup></FieldGroup>
      </FieldSet>
    </div>
  );
}
