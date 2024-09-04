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

export default function NavBar() {
  const [open, setOpen] = useState(false);
  const [mode, setMode] = useState("Day")
  const toggleMenu = (newOpen) => () => {
    setOpen(newOpen);
  };
  const navigate = useNavigate();
  useEffect(() => {
    switch (mode) {
      case "Day" :
        navigate("/app/timelog");
        break;
      case "Week" :
        navigate("/app/weekview");
        break;
      case "Month" :
        navigate("/app/monthview");
        break;
    }
  }, [mode])

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
      <Toolbar variant="dense">
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
          className="mx-8 bg-white h-8"
          size="small"
          inputProps={{"aria-label": "Without label"}}
          value={mode}
          onChange={(event) => {
            setMode(event.target.value);
          }}
          autoWidth
        >
          <MenuItem value="Day">Day</MenuItem>
          <MenuItem value="Week">Week</MenuItem>
          <MenuItem value="Month">Month</MenuItem>
        </Select>
      </Toolbar>
      <Drawer open={open} onClose={toggleMenu(false)}>
        {DrawerList}
      </Drawer>
    </AppBar>
  );
}