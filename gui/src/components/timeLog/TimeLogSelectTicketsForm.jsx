import {Checkbox, FormControl, ListItemText, MenuItem, Select} from "@mui/material";

export default function TimeLogSelectTicketsForm({filterTickets, selectedTickets, setSelectedTickets}) {

  return (
    <FormControl className="mx-2">
      <Select
        size="small"
        multiple
        value={selectedTickets}
        onChange={(event) => setSelectedTickets(event.target.value)}
        renderValue={(selected) => (
          selected.length > 0 ? selected.join(", ") : <em>Select tickets</em>
        )}
        displayEmpty
      >
        {filterTickets.map((ticket) => (
          <MenuItem key={ticket} value={ticket}>
            <Checkbox size="small" checked={selectedTickets.indexOf(ticket) > -1} />
            <ListItemText primary={ticket} />
          </MenuItem>
        ))}
      </Select>
    </FormControl>
  )
}