import {Tooltip} from "@mui/material";
import WarningAmberIcon from "@mui/icons-material/WarningAmber.js";
import TimeLogJiraSyncStatusIcon from "./TimeLogJiraSyncStatusIcon.jsx";
import useAppContext from "../context/useAppContext.js";
import {deepOrange} from "@mui/material/colors";

export default function TimeLogStatusIcons({isConflicted, jiraSyncStatus, showOnlyNotSuccessfullySynced}) {
  const {isJiraSyncingEnabled} = useAppContext();
  return (
    <>
      {isConflicted && <Tooltip className="mr-1" title="Conflicted"><WarningAmberIcon sx={{color: deepOrange[200]}} className="text-red" /></Tooltip>}
      {isJiraSyncingEnabled && <TimeLogJiraSyncStatusIcon status={jiraSyncStatus} showOnlyNotSuccessfullySynced={showOnlyNotSuccessfullySynced} /> }
    </>
  )
}