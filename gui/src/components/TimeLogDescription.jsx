import {LinearProgress, TextField} from "@mui/material";
import {useEffect, useRef, useState} from "react";
import useAsyncCall from "../hooks/useAsyncCall.js";
import classNames from "classnames";
import ResetButton from "./ResetButton.jsx";
import SaveButton from "./SaveButton.jsx";

export default function TimeLogDescription({description, ids, setGroupDescription, className}) {
  const [isEditing, setIsEditing] = useState(false);
  const [descriptionField, setDescriptionField] = useState(description)
  const descriptionFieldRef = useRef(null);

  const {execute: handleSetGroupDescription, isExecuting} = useAsyncCall({
    fn: setGroupDescription,
    onError: resetChanges,
  })

  function resetChanges() {
    setDescriptionField(description);
    setIsEditing(false)
  }

  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [descriptionFieldRef.current, description, descriptionField]);

  function handleClickOutside(event) {
    if (descriptionFieldRef.current && !descriptionFieldRef.current.contains(event.target)) {
      setIsEditing(false);
      if (isModified) {
        handleSetGroupDescription({ids, description: descriptionField})
      }
    }
  }

  const isModified = description !== descriptionField;

  return (
    <div
      ref={descriptionFieldRef}
      className={classNames(className, "text-justify whitespace-pre-wrap", {
        "w-full py-2": isEditing,
        "hover:bg-blue-100": !isEditing,
      })}
    >
      {isEditing ? (
        <div>
          <div className="flex items-center">
            <TextField
              className="w-full"
              label="Description"
              value={descriptionField}
              onChange={(event) => setDescriptionField(event.target.value)}
              size="small"
              autoComplete="off"
              multiline
              onFocus={(e) => {
                const value = e.target.value
                e.target.setSelectionRange(value.length, value.length);
              }}
              autoFocus
            />
            <ResetButton onReset={resetChanges} />
            <SaveButton
              onSave={() => handleSetGroupDescription({ids, description: descriptionField})}
              className="mr-2 p-0"
              disabled={!isModified}
            />
          </div>
        </div>
      ) : (
        <div
          onMouseDown={(event) => {
            event.preventDefault()
            setIsEditing(true);
          }}
          className={`text-justify whitespace-pre-wrap ${!description ? "text-gray-500" : ""}`}>{description ? description : "add description..."}</div>
      )}
      {isExecuting && <LinearProgress className="mt-1" />}

    </div>
  )
}