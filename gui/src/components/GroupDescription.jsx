import {TextField} from "@mui/material";
import {useEffect, useRef, useState} from "react";

export default function GroupDescription({description, ids, setGroupDescription}) {
  const [isEditing, setIsEditing] = useState(false);
  const [descriptionField, setDescriptionField] = useState(description)
  const descriptionFieldRef = useRef(null);

  useEffect(() => {
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [descriptionFieldRef.current, descriptionField]);
  function handleClickOutside(event) {
    if (descriptionFieldRef.current && !descriptionFieldRef.current.contains(event.target)) {
      setIsEditing(false);
      if (isModified) {
       setGroupDescription({ids, description: descriptionField})
      }
    }
  }
  const isModified = description !== descriptionField;

  return (
    <div
      ref={descriptionFieldRef}
      className={`text-justify whitespace-pre-wrap mx-4 ${isEditing ? "w-full" : "hover:bg-blue-100"}`}
      onClick={() => {
        setIsEditing(true);
      }}
    >
      {isEditing ? (
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
      ) : (
        <div className="text-justify whitespace-pre-wrap">{description}</div>
      )}
    </div>
  )
}