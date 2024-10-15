import {TimeField} from "@mui/x-date-pickers";

export default function CustomTimeField({name, label, value, setValue, error, setError, getNewValue, className}) {
  function validateTimeFields(newTime, setError) {
    if (newTime === null || (newTime.isValid && newTime.isValid())) {
      setError(false);
    } else {
      setError(true);
    }
  }

  return (
    <TimeField
      name={name}
      error={error}
      className={`w-20 ${className}`}
      label={label}
      size="small"
      value={value}
      onChange={(timeToSet) => {
        validateTimeFields(timeToSet, setError);
        if (timeToSet === null) {
          setValue(null);
        } else if (timeToSet.isValid()) {
          setValue(getNewValue(timeToSet));
        }
      }}
      format="HH:mm"
    />
  );

}