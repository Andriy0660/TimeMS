import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import SyncDisabledIcon from '@mui/icons-material/SyncDisabled';
import {syncStatus as syncStatusObj} from "../consts/syncStatus.js";
import {SyncProblem} from "@mui/icons-material";

export default function TimeLogStatusIcons({isConflicted, syncStatus}) {
  return (
    <>
      {syncStatus === syncStatusObj.PARTIAL_SYNCED && <Tooltip title="Partial synchronized"><SyncProblem size="large" color="warning" className="mr-1 text-xl" /></Tooltip>}
      {syncStatus === syncStatusObj.NOT_SYNCED && <Tooltip title="Not synchronized"><SyncDisabledIcon size="large" color="error" className="mr-1 text-xl" /></Tooltip>}
      {isConflicted && <Tooltip title="Conflicted"><WarningAmberIcon size="small" color="error" className="mr-1 text-xl" /></Tooltip>}
    </>
  )
}