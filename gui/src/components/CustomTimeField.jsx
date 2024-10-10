import {TimeField} from "@mui/x-date-pickers";

export default function CustomTimeField({name, label, value, setValue, error, setError, getNewValue}) {
  function validateTimeFields(newTime, setError) {
    if (newTime === null || (newTime.isValid && newTime.isValid())) {
      setError(false);
    } else {
      setError(true);
    }
  }

  return (
    <div className="mr-4">
      <TimeField
        name={name}
        error={error}
        className="w-20"
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
    </div>
  );

}