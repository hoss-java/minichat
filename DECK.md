---
Title: minichat
Description: plans and project management sheets
Date: 
Robots: noindex,nofollow
Template: index
---

# minichat

## Analyzing all parts

|#|Part|Details|Total Duration|Status|
|:-|:-|:-|:-|:-|
|1|-|-|-|-|-|
|:-|:-|:-|::||


## Timeplan

```mermaid
gantt
    section %BOARD%
```

# 1 - minichat

## 001-0001
> **Setup repository.** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> The goal of this card is to setup/initialize minichat repository.
> 
> # DOD (definition of done):
> - Needed beranches are created.
> - Deck and pm are setup.
> - Needed files are created.
> 
> # TODO:
> - [x] 1. Create branches, main and develop
> - [x] 2. Setup deck
> - [x] 3. Create this card!
> - [x] 4. Create needed files such gitignore
> - [x] 5. Add an empty README
> 
> # Reports:
> * Steps to create repo
> > 1.  Create a new repo on Github (the main branch)
> > 2. Colon it
> >>```
> >>git clone git@github.com:hoss-java/<repo>.git
> >>```
> > 3. Add `gitignore`
> > 4. Create a new develop branch
> >>```
> >>git checkout -b develop
> >>```
> > 5. Install `git-deck`
> > 6. Intialize deck
> >>```
> >>git config alias.deck '!bash .git/hooks/git-deck/deck'
> >>
> >>git deck pm init
> >>git deck pm edit
> >>git deck pm initdeck
> >>git deck board mk minichat
> >>```
> > 7. Add the first card, and move it to ONGOING
> >>```
> >>git deck card mk "Setup repo."
> >>git deck card mv 0001 ONGOING
> >>```
> > 8. The first commit
> >>```
> >>git add -A
> >>git commit -m "[B001-C0001] Setup repository."
> >># git commit -m "[B001-C0001] Setup repository." -m "--merge"
> >>#git commit --amend
> >>```
> * Needed files
> >```
> >.commit-check
> >.container-run
> >container-run
> >.gitdefault
> >.gitignore
> >.prompt
> >```
> * Add an empty README.md
> </details>

## 001-0002
> **Configuer github workflows.** ![status](https://img.shields.io/badge/status-DONE-brightgreen)
> <details >
>     <summary>Details</summary>
> The goal of this card is to configure github workflows for this repository.
> 
> # DOD (definition of done):
> 
> # TODO:
> - [] 1.
> 
> # Reports:
> *
> </details>
