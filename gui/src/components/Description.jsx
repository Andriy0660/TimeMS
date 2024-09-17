import {IconButton, LinearProgress, TextField, Tooltip} from "@mui/material";
import {useEffect, useRef, useState} from "react";
import BackspaceOutlinedIcon from "@mui/icons-material/BackspaceOutlined.js";
import SaveOutlinedIcon from "@mui/icons-material/SaveOutlined.js";
import useAsyncCall from "../hooks/useAsyncCall.js";

export default function Description({description, ids, setGroupDescription, className}) {
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
      className={`${className} text-justify whitespace-pre-wrap ${isEditing ? "w-full mt-2" : "hover:bg-blue-100"}`}
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

            <Tooltip
              title="Reset">
              <IconButton
                onClick={resetChanges}
                className="ml-2">
                <BackspaceOutlinedIcon fontSize="small" />
              </IconButton>
            </Tooltip>

            <Tooltip title="Save">
            <span>
              <IconButton
                onClick={() => handleSetGroupDescription({ids, description: descriptionField})}
                color="success"
                disabled={!isModified}
              >
                <SaveOutlinedIcon fontSize="small" />
              </IconButton>
            </span>
            </Tooltip>
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