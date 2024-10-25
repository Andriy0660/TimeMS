import {IconButton, TextField, Tooltip} from "@mui/material";
import AddIcon from "@mui/icons-material/Add.js";
import {useState} from "react";
import useAppContext from "../../context/useAppContext.js";

export default function TimeLogLabelEditor({className, isLabelAdding, setIsLabelAdding, isHovered, timeLog, handleUpdateTimeLog}) {
  const [labelToAdd, setLabelToAdd] = useState("");
  const {addAlert} = useAppContext();

  return (
    <div className={`${className} flex items-center`}>
      {isHovered && !isLabelAdding && (
        <Tooltip title="Add label">
          <IconButton
            onClick={() => setIsLabelAdding(true)}
            color="primary"
            className="p-0"
          >
            <AddIcon />
          </IconButton>
        </Tooltip>
      )}
      {isLabelAdding && (
        <TextField
          className="w-24"
          autoFocus
          label="Label"
          size="small"
          value={labelToAdd}
          onChange={(event) => setLabelToAdd(event.target.value)}
          onKeyDown={async (e) => {
            if (e.key === "Enter") {
              if (timeLog.labels.includes(labelToAdd)) {
                addAlert({
                  text: "This label is already present",
                  type: "warning"
                });
              } else {
                await handleUpdateTimeLog({
                  id: timeLog.id,
                  ticket: timeLog.ticket,
                  startTime: timeLog.startTime,
                  endTime: timeLog.endTime,
                  labels: [...timeLog.labels, labelToAdd]
                });
              }
              setLabelToAdd("");
              setIsLabelAdding(false);
            }
          }}
          autoComplete="off"
        />
      )}
    </div>
  )
}