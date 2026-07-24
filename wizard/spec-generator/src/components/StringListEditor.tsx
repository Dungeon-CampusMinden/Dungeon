import { ArrowDownIcon, ArrowUpIcon, PlusIcon, TrashIcon } from "lucide-react";
import { Button } from "./ui/button";
import { Input } from "./ui/input";
import { Textarea } from "./ui/textarea";
import { Tooltip, TooltipContent, TooltipTrigger } from "./ui/tooltip";

export function StringListEditor({
  value,
  onChange,
  lockOrder = false,
  useTextarea = false,
  preventEmpty = false,
}: {
  value: string[];
  onChange: (newValue: string[]) => void;
  lockOrder?: boolean;
  useTextarea?: boolean;
  preventEmpty?: boolean;
}) {
  const handleAdd = () => {
    onChange([...value, ""]);
  };

  const handleRemove = (index: number) => {
    if (preventEmpty && value.length === 1) return;
    const newValue = [...value];
    newValue.splice(index, 1);
    onChange(newValue);
  };

  const handleChange = (index: number, newValue: string) => {
    const updatedValues = [...value];
    updatedValues[index] = newValue.trimStart();
    onChange(updatedValues);
  };

  const handleMoveUp = (index: number) => {
    if (index <= 0) return;
    const newValue = [...value];
    [newValue[index - 1], newValue[index]] = [newValue[index], newValue[index - 1]];
    onChange(newValue);
  };

  const handleMoveDown = (index: number) => {
    if (index >= value.length - 1) return;
    const newValue = [...value];
    [newValue[index + 1], newValue[index]] = [newValue[index], newValue[index + 1]];
    onChange(newValue);
  };

  return (
    <div className="flex flex-col gap-2">
      <Button onClick={handleAdd} className="lg:max-w-40">
        <PlusIcon />
        Neuer Eintrag
      </Button>
      <div className="flex flex-col gap-2">
        {value.map((item, index) => {
          const canMoveUp = index > 0;
          const canMoveDown = index < value.length - 1;
          const canRemove = !(preventEmpty && value.length === 1);
          return (
            <div key={index} className="grid grid-cols-[1fr_auto] items-center gap-1">
              {useTextarea ? (
                <Textarea value={item} onChange={(e) => handleChange(index, e.target.value)} />
              ) : (
                <Input value={item} onChange={(e) => handleChange(index, e.target.value)} />
              )}
              <div className="flex items-center gap-0.5">
                {!lockOrder && (
                  <>
                    <Button variant="outline" onClick={() => handleMoveUp(index)} disabled={!canMoveUp}>
                      <ArrowUpIcon />
                    </Button>
                    <Button variant="outline" onClick={() => handleMoveDown(index)} disabled={!canMoveDown}>
                      <ArrowDownIcon />
                    </Button>
                  </>
                )}
                <Button variant="destructive" onClick={() => handleRemove(index)} disabled={!canRemove}>
                  <TrashIcon />
                </Button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export function ObjectListStringEditor({
  value,
  onChange,
  getItemText,
  setItemText,
  produceItem,
  lockOrder = false,
  useTextarea = false,
  preventEmpty = false,
}: {
  value: any[];
  onChange: (newValue: any[]) => void;
  getItemText: (item: any) => string;
  setItemText: (item: any, text: string) => void;
  produceItem: () => any;
  lockOrder?: boolean;
  useTextarea?: boolean;
  preventEmpty?: boolean;
}) {
  const handleAdd = () => {
    onChange([...value, produceItem()]);
  };

  const handleRemove = (index: number) => {
    if (preventEmpty && value.length === 1) return;
    const newValue = [...value];
    newValue.splice(index, 1);
    onChange(newValue);
  };

  const handleChange = (index: number, newValue: string) => {
    const newValueArray = [...value];
    setItemText(newValueArray[index], newValue);
    onChange(newValueArray);
  };

  const handleMoveUp = (index: number) => {
    if (index <= 0) return;
    const newValue = [...value];
    [newValue[index - 1], newValue[index]] = [newValue[index], newValue[index - 1]];
    onChange(newValue);
  };

  const handleMoveDown = (index: number) => {
    if (index >= value.length - 1) return;
    const newValue = [...value];
    [newValue[index + 1], newValue[index]] = [newValue[index], newValue[index + 1]];
    onChange(newValue);
  };

  const canRemove = value.length > 1 || !preventEmpty;

  return (
    <div className="flex flex-col gap-2">
      <Button onClick={handleAdd} className="lg:max-w-40">
        <PlusIcon />
        Neuer Eintrag
      </Button>
      <div className="flex flex-col gap-2">
        {value.map((item, index) => {
          const canMoveUp = index > 0;
          const canMoveDown = index < value.length - 1;
          return (
            <div key={index} className="grid grid-cols-[1fr_auto] items-center gap-1">
              {useTextarea ? (
                <Textarea value={getItemText(item)} onChange={(e) => handleChange(index, e.target.value)} />
              ) : (
                <Input value={getItemText(item)} onChange={(e) => handleChange(index, e.target.value)} />
              )}
              <div className="flex items-center gap-0.5">
                {!lockOrder && (
                  <>
                    <Button variant="outline" onClick={() => handleMoveUp(index)} disabled={!canMoveUp}>
                      <ArrowUpIcon />
                    </Button>
                    <Button variant="outline" onClick={() => handleMoveDown(index)} disabled={!canMoveDown}>
                      <ArrowDownIcon />
                    </Button>
                  </>
                )}
                <Button variant="destructive" onClick={() => handleRemove(index)} disabled={!canRemove}>
                  <TrashIcon />
                </Button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
