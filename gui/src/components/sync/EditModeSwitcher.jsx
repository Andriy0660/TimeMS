import {FormControlLabel, Switch} from "@mui/material";

export default function EditModeSwitcher({checked, setChecked, allEditModeStates, serviceName}) {
  return (
    <FormControlLabel
      control={
        <Switch
          checked={checked}
          onChange={(event) => {
            if (event.target.checked) {
              setChecked(true);
              allEditModeStates.forEach((editModeState) => editModeState.setChecked !== setChecked && editModeState.setChecked(false))
            } else {
              setChecked(false);
            }
          }}
        />
      }
      label={`${serviceName} Edit Mode`}
      labelPlacement="start"
    />
  )
}