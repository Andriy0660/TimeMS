import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import CloseIcon from "@mui/icons-material/Close";

export default function StatusIcon({isConflicted, isSynced}) {
  return (
    <>
      {isSynced === false && <Tooltip title="Not synchronized"><CloseIcon size="large" color="error" className="mr-1 text-xl" /></Tooltip>}
      {isConflicted && <Tooltip title="Conflicted"><WarningAmberIcon size="small" color="error" className="mr-1 text-xl" /></Tooltip>}
    </>
  )
}