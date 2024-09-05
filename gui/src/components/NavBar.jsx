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
  Typography
} from "@mui/material";
import MenuIcon from '@mui/icons-material/Menu';
import {useEffect, useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import MonthPicker from "./MonthPicker..jsx";
import WeekPicker from "./WeekPicker.jsx";
import DayPicker from "./DayPicker.jsx";
import useAppContext from "../context/useAppContext.js";
import useDateInUrl from "../hooks/useDateInUrl.js";

export default function NavBar() {
  const [open, setOpen] = useState(false);
  const queryParams = new URLSearchParams(location.search);
  const {date} = useAppContext();
  const [view, setView] = useState(queryParams.get("view") || "Day")
  const navigate = useNavigate();

  useDateInUrl(date);

  useEffect(() => {
    const viewInUrl = queryParams.get("view") || "Day";
    if (viewInUrl !== view) {
      setView(queryParams.get("view") || "Day")
    }
  }, [location.search]);

  useEffect(() => {
    let viewUrl;
    switch (view) {
      case "Day" :
        viewUrl = "/app/timelog";
        break;
      case "Week" :
        viewUrl = "/app/weekview";
        break;
      case "Month" :
        viewUrl = "/app/monthview";
        break;
    }
    const params = new URLSearchParams(location.search);
    params.set("view", view);
    navigate({pathname: viewUrl, search: params.toString()});
  }, [view])

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
            setView(event.target.value);
          }}
          autoWidth
        >
          <MenuItem value="Day">Day</MenuItem>
          <MenuItem value="Week">Week</MenuItem>
          <MenuItem value="Month">Month</MenuItem>
        </Select>
        {modeDatePickerConfig[view]}

      </Toolbar>
      <Drawer open={open} onClose={toggleMenu(false)}>
        {DrawerList}
      </Drawer>
    </AppBar>
  );
}