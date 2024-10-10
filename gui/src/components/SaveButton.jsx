import {IconButton, Tooltip} from "@mui/material";
import SaveOutlinedIcon from "@mui/icons-material/SaveOutlined.js";

export default function SaveButton({onSave, isDisabled, className}) {
  return (
    <Tooltip title="Save">
      <span>
        <IconButton
          onClick={onSave}
          className={className}
          color="success"
          disabled={isDisabled}
        >
          <SaveOutlinedIcon fontSize="small" />
        </IconButton>
      </span>
    </Tooltip>
  )
}