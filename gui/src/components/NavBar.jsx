import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  MenuItem,
  Select,
  Toolbar,
  Tooltip,
  Typography
} from "@mui/material";
import MenuIcon from '@mui/icons-material/Menu';
import {useState} from "react";
import {Link} from "react-router-dom";
import MonthPicker from "./MonthPicker..jsx";
import WeekPicker from "./WeekPicker.jsx";
import DayPicker from "./DayPicker.jsx";
import useAppContext from "../context/useAppContext.js";
import useDateInUrl from "../hooks/useDateInUrl.js";
import dayjs from "dayjs";
import SettingsBackupRestoreIcon from "@mui/icons-material/SettingsBackupRestore.js";
import useViewChanger from "../hooks/useViewChanger.js";

export default function NavBar() {
  const [open, setOpen] = useState(false);
  const {date, setDate, view} = useAppContext();

  useDateInUrl(date);
  const {changeView} = useViewChanger();

  const modeDatePickerConfig = {
    Day: <DayPicker isOnNavBar />,
    Week: <WeekPicker isOnNavBar />,
    Month: <MonthPicker isOnNavBar />,
    All: null,
  };

  const toggleMenu = (newOpen) => () => {
    setOpen(newOpen);
  };

  const DrawerList = (
    <Box sx={{width: 250}} onClick={toggleMenu(false)}>
      <List>
        <ListItem disablePadding>
          <Link to="/app/timelog" className="text-inherit no-underline w-full">
            <ListItemButton>
              <ListItemText primary="TimeLog" />
            </ListItemButton>
          </Link>
        </ListItem>

        <ListItem disablePadding>
          <Link to="/app/info" className="text-inherit no-underline w-full">
            <ListItemButton>
              <ListItemText primary="Info" />
            </ListItemButton>
          </Link>
        </ListItem>
      </List>

    </Box>
  );

  return (
    <AppBar position="static">
      <Toolbar>
        <IconButton
          onClick={toggleMenu(true)}
          size="large"
          edge="start"
          sx={{mr: 2}}
          color="inherit"
        >
          <MenuIcon />
        </IconButton>
        <Typography variant="h6">
          Time Craft
        </Typography>
        <Select
          className="mx-8 bg-white"
          size="small"
          inputProps={{"aria-label": "Without label"}}
          value={view}
          onChange={(event) => {
            changeView(event.target.value);
          }}
          autoWidth
        >
          <MenuItem value="Day">Day</MenuItem>
          <MenuItem value="Week">Week</MenuItem>
          <MenuItem value="Month">Month</MenuItem>
        </Select>
        {modeDatePickerConfig[view]}
        <Tooltip title="reset">
          <IconButton
            onClick={() => setDate(dayjs())}
            variant="outlined"
            className="text-white"
          >
            <SettingsBackupRestoreIcon />
          </IconButton>
        </Tooltip>
      </Toolbar>
      <Drawer open={open} onClose={toggleMenu(false)}>
        {DrawerList}
      </Drawer>
    </AppBar>
  );
}