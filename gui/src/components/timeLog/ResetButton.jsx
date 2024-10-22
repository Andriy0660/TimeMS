import {IconButton, Tooltip} from "@mui/material";
import BackspaceOutlinedIcon from "@mui/icons-material/BackspaceOutlined.js";

export default function ResetButton({onReset, className}) {
  return (
    <Tooltip title="Reset">
      <IconButton onClick={onReset} className={className}>
        <BackspaceOutlinedIcon fontSize="small" />
      </IconButton>
    </Tooltip>
  )
}