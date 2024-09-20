import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import PendingIcon from "@mui/icons-material/Pending.js";
import CloseIcon from "@mui/icons-material/Close";

export default function StatusIcon({isConflicted, isInProgress, isSynced}) {
  if (isSynced) return <Tooltip title="Not synchronized"><CloseIcon color="error" className="mr-1" /></Tooltip>

  if (isConflicted) return <Tooltip title="Conflicted"><WarningAmberIcon color="error" className="mr-1" /></Tooltip>

  if (isInProgress) return <Tooltip title="In Progress"><PendingIcon color="primary" className="mr-1" /></Tooltip>

}