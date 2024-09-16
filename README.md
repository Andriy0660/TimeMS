## Import and Export Format

The import and export functionality supports the following format:

### [+ or -] startTime - endTime (duration) - [ticketId] description

## Symbols Explanation

- **+** - The task is included in the working hours.
- **-** - The task is not included in the working hours.

## Fields Explanation

### `startTime`

- The start time of the task in the format `HH:mm` (e.g., 12:00, 23:59, etc.).
- If the start time is undefined - `**:**`.

### `endTime`

- The end time of the task in the format `HH:mm` (e.g., 12:00, 23:59, etc.).
- If the end time is undefined - `**:**`.

### `duration`

- The duration of the task in the format `HH:mm` (e.g., 1:30 means the task lasted one hour and thirty minutes).
- If the duration is undefined (for example, if the end time is not specified) - `**:**`.

### `ticketId`

- The `issueKey` of the specific task in the format [TST-1].
- If the `ticketId` is undefined - `[???]`.

### `description`

- A text description of the task.