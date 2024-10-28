import {useState} from "react";
import ClearIcon from "@mui/icons-material/Clear";
import {IconButton, Tooltip} from "@mui/material";

export default function TimeLogLabel({label, onRemove, className}) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      className={`px-1 truncate bg-blue-100 text-blue-700 shadow-md rounded-md ${className}`}
    >
      {isHovered && (
        <Tooltip title="Delete">
          <IconButton
            onClick={onRemove}
            color="error"
            className={`p-0 mr-1 ${isHovered ? "cursor-pointer" : ""}`}
          >
            <ClearIcon size="small" className="text-lg"/>
          </IconButton>
        </Tooltip>
      )}
      {label}
    </div>

  )
}