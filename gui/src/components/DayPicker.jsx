import ArrowBackIosIcon from '@mui/icons-material/ArrowBackIos';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';
import Button from "@mui/material/Button";
import {MobileDatePicker} from "@mui/x-date-pickers";

export default function DayPicker({date, setDate, isPlaceholderData}) {
  return (
    <div className="flex items-center justify-center">
      <Button
        onClick={() => {
          setDate(date.subtract(1, "day"))
        }}
        disabled={isPlaceholderData}
      >
        <ArrowBackIosIcon />
      </Button>
      <MobileDatePicker
        slotProps={{textField: {size: "small"}}}
        className="w-32"
        value={date}
        onChange={(newValue) => setDate(newValue)}
        format="DD/MM/YYYY"
      />
      <Button
        onClick={() => {
          setDate(date.add(1, "day"))
        }}
        disabled={isPlaceholderData}
      >
        <ArrowForwardIosIcon />
      </Button>
    </div>
  );
}