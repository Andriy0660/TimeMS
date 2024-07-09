const {execSync} = require('child_process');

// Get environment variables
const branchName = process.env.BRANCH_NAME;
const baseRef = process.env.BASE_REF;

// Extract Jira task number from branch name
const jiraTaskNumberMatch = branchName.match(/([A-Z]+-[0-9]+)/);
if (!jiraTaskNumberMatch) {
  console.error("Branch name does not contain a valid Jira task number.");
  process.exit(1);
}
const jiraTaskNumber = jiraTaskNumberMatch[1];

// Fetch base branch
execSync(`git fetch origin ${baseRef}:${baseRef}`);

// Determine base commit
const baseCommit = execSync(`git merge-base HEAD ${baseRef}`).toString().trim();

// List commit messages
const commitMessages = execSync(`git log --pretty=format:"%H %s" ${baseCommit}..HEAD`).toString().trim().split('\n');

for (const commit of commitMessages) {
  const [commitHash, ...commitMessageArray] = commit.split(' ');
  const commitMessage = commitMessageArray.join(' ');

  // Skip merge commits
  const parents = execSync(`git show --no-patch --format="%P" ${commitHash}`).toString().trim();
  if (parents.includes(' ')) {
    continue;
  }

  // Check if the commit message contains the ticket number
  if (!commitMessage.toLowerCase().includes(jiraTaskNumber.toLowerCase())) {
    console.error(`Commit ${commitHash} message does not contain ticket number ${jiraTaskNumber}: ${commitMessage}`);
    process.exit(1);
  }
}
