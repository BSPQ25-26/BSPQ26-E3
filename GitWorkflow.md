# Git Workflow Guide

> This document defines the branching strategy, commit conventions, and pull request process for this repository. All team members must follow these rules.

---

## Table of Contents

- [Branch Naming Convention](#branch-naming-convention)
- [Branch Flow](#branch-flow)
- [Creating a Branch](#creating-a-branch)
- [Writing Good Commits](#writing-good-commits)
- [Opening a Pull Request](#opening-a-pull-request)
- [Merging Rules](#merging-rules)
- [Cleaning Up Branches](#cleaning-up-branches)
- [Quick Reference](#quick-reference)

---

## Branch Naming Convention

All branches must follow this format:

```
<initials>/<type>/<description>
```

| Part | Description | Example |
|---|---|---|
| `<initials>` | Your name initials (2–3 letters, lowercase) | `jd`, `mrc` |
| `<type>` | The type of change (see table below) | `feat`, `fix` |
| `<description>` | Short, hyphen-separated description of the task | `user-login`, `fix-null-error` |

### Allowed Types

| Type | Use for |
|---|---|
| `feat` | A new feature |
| `fix` | A bug fix |
| `hotfix` | An urgent fix that needs immediate release |
| `refactor` | Code restructuring with no functional changes |
| `chore` | Maintenance tasks, dependency updates, config |
| `docs` | Documentation only changes |
| `test` | Adding or updating tests |
| `style` | Formatting, linting, whitespace (no logic change) |

### Examples

```
jd/feat/user-authentication
mrc/fix/null-pointer-on-login
ab/refactor/clean-up-api-service
jd/docs/update-readme
```

---

## Branch Flow

The flow is **always one direction**:

```
<initials>/<type>/<description>  →  main
```

- **Your branch** is where you do all your work.
- **`main`** is the production branch — only stable, reviewed code reaches here.

> ⚠️ **Direct commits to `main` are not allowed.** All changes must go through a Pull Request.

---

## Creating a Branch

Always branch off from `main`.

```bash
# 1. Make sure you have the latest main
git checkout main
git pull origin main

# 2. Create your branch
git checkout -b jd/feat/user-authentication

# 3. Push it to the remote to make it visible to the team
git push -u origin jd/feat/user-authentication
```

---

## Writing Good Commits

### Commit Message Format

```
<type>: <short description>

[optional body]

[optional footer]
```

- The **first line** (subject) must be under **72 characters**.
- Use the **imperative mood**: `add`, `fix`, `update` — not `added`, `fixed`, `updated`.
- The **body** (optional) explains *what* and *why*, not *how*.
- The **footer** (optional) references tickets or breaking changes.

### Examples

✅ Good commits:

```
feat: add JWT authentication to login endpoint

Implemented token-based auth using jsonwebtoken.
Tokens expire after 24h. Refresh tokens are stored in Redis.

Closes #42
```

```
fix: prevent null pointer when user profile is missing
```

```
chore: update eslint to v9 and fix rule conflicts
```

❌ Bad commits:

```
fixed stuff
WIP
changes
asdfgh
update
```

### Commit Tips

- **Commit often, commit small.** One logical change per commit.
- **Never commit broken code** to a shared branch.
- **Don't commit secrets**, API keys, or `.env` files — ever.
- Stage carefully: use `git add -p` to review what you're committing.

---

## Opening a Pull Request

### Step 1 — Make sure your branch is up to date

Before opening a PR, sync with `main` to avoid conflicts:

```bash
git checkout main
git pull origin main
git checkout jd/feat/user-authentication
git rebase main
# or: git merge main
git push origin jd/feat/user-authentication
```

### Step 2 — Open the PR on GitHub / GitLab

- **Base branch:** `main`
- **Title:** Follow the same format as commits → `feat: add user authentication`
- **Description:** Fill in the PR template (see below)

### PR Description Template

```markdown
## What does this PR do?
Brief summary of the change and why it was made.

## Type of change
- [ ] feat – new feature
- [ ] fix – bug fix
- [ ] refactor – no functional change
- [ ] chore / docs / test / style

## How to test
Steps to verify the change works correctly.

## Related issues
Closes #<issue_number>

## Checklist
- [ ] My code follows the project's coding standards
- [ ] I have added/updated tests where necessary
- [ ] I have updated documentation if needed
- [ ] The branch is rebased / up to date with `main`
```

### Step 3 — Request a review

- Assign **at least one reviewer**.
- Do not merge your own PR without approval.
- Address all review comments before merging.

---

## Merging Rules

| Target | Allowed? | How |
|---|---|---|
| `main` | ✅ Only from feature branches | PR with approval |
| `main` | ❌ Direct commit | Not allowed (branch protection) |

- Use **Squash and Merge** or **Merge Commit** — follow your team's agreed convention.
- Delete the source branch after merging.

---

## Cleaning Up Branches

Stale branches create noise and confusion. Clean them up regularly — after a PR is merged, the branch should go.

### Delete your local branch

```bash
# Switch away from the branch first
git checkout main

# Delete a branch that has already been merged
git branch -d jd/feat/user-authentication

# Force delete a branch (use only if you're sure you don't need it)
git branch -D jd/feat/user-authentication
```

> `-d` is safe — it refuses to delete if the branch hasn't been merged. `-D` forces it regardless.

### Delete the remote branch

```bash
git push origin --delete jd/feat/user-authentication
```

### Clean up remote tracking references

Over time your local git accumulates references to remote branches that no longer exist. Remove them with:

```bash
git fetch --prune
# or shorter:
git fetch -p
```

You can also set this to happen automatically on every fetch:

```bash
git config --global fetch.prune true
```

### See all merged branches (before deleting)

```bash
# Local branches already merged into main
git branch --merged main

# Remote branches already merged into main
git branch -r --merged main
```

### Bulk delete all local branches already merged into main

```bash
# Preview first — make sure the list looks right
git branch --merged main | grep -v -E "^\*|main"

# Then delete them all
git branch --merged main | grep -v -E "^\*|main" | xargs git branch -d
```

> ⚠️ Always preview the list before running the bulk delete. The `grep -v` excludes `main` from deletion.

---

## Quick Reference

```bash
# Start a new task
git checkout main && git pull origin main
git checkout -b <initials>/<type>/<description>
git push -u origin <initials>/<type>/<description>

# Daily sync with main
git fetch origin
git rebase origin/main

# Commit
git add -p                          # review changes carefully
git commit -m "feat: short description"

# Before opening PR — sync one last time
git rebase origin/main
git push origin <your-branch>

# Open PR on GitHub/GitLab → base: main

# After PR is merged — clean up
git checkout main
git pull origin main
git branch -d <your-branch>                   # delete local branch
git push origin --delete <your-branch>        # delete remote branch
git fetch --prune                             # remove stale remote refs
```

---

> Questions? Raise them in the team channel before breaking any of these rules. 🙂
