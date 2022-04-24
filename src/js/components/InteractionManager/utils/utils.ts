import React from "react";

// Helpers for the InteractionManager
export const getClosestBlock = (e) => e?.target?.closest('.block-container');
export const getIsTargetInBlockActions = (e) => e?.target?.closest('block-actions');
export const getIsTargetPageLink = (e) => e?.target?.classList?.contains('link');
export const getIsTargetUrlLink = (e) => e?.target?.classList?.contains('url-link');
export const getIsTargetHashtag = (e) => e?.target?.classList?.contains('hashtag');
export const getIsTargetAutolink = (e) => e?.target?.classList?.contains('autolink');
export const getIsTargetBlockRef = (e) => e?.target?.classList?.contains('block-ref');

interface TargetPos {
  x: number;
  y: number;
}

/* 
  Returns an attibute of the event target if the
  target meets a provided condition.
  Returns false if the target does not meet the condition.
  Returns the attribute if the target meets the condition.
*/
export const attrIf = (e: React.MouseEvent<HTMLElement>, condition: (e: React.MouseEvent<HTMLElement>) => boolean, attr: string): string | false => {
  if ((e.target) && condition(e)) {
    const target = e.target as HTMLElement;
    return target.getAttribute(attr)
  };
  return false;
};

const posOfPointer = (e: React.MouseEvent<HTMLElement>): TargetPos => ({ x: e.clientX + 10, y: e.clientY + 10 });
const posOfTargetBottomEnd = (e: React.MouseEvent<HTMLElement>): TargetPos => {
  const target = e.target as HTMLElement;
  const rect = target.getBoundingClientRect() as DOMRect;
  return { x: rect.left + rect.width, y: rect.top + rect.height };
};

const posOfTargetBottomStart = (e: React.MouseEvent<HTMLElement>): TargetPos => {
  const target = e.target as HTMLElement;
  const rect = target.getBoundingClientRect() as DOMRect;
  return { x: rect.left, y: rect.top + rect.height };
};

// 
export const updatePreview = (e: React.MouseEvent<HTMLElement>, setPreviewPos, setPreview) => {

  const previewedPage = attrIf(e, getIsTargetPageLink, 'data-page-title');
  if (previewedPage) {
    setPreview(previewedPage, 'page');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedHashtag = attrIf(e, getIsTargetHashtag, 'data-page-title');
  if (previewedHashtag) {
    setPreview(previewedHashtag, 'page');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedBlockRef = attrIf(e, getIsTargetBlockRef, 'data-uid');
  if (previewedBlockRef) {
    setPreview(previewedBlockRef, 'block');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedUrl = attrIf(e, getIsTargetUrlLink, 'href');
  if (previewedUrl) {
    setPreview(previewedUrl, 'url');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  const previewedAutoUrl = attrIf(e, getIsTargetAutolink, 'href');
  if (previewedAutoUrl) {
    setPreview(previewedAutoUrl, 'url');
    setPreviewPos(posOfTargetBottomStart(e));
    return;
  }

  setPreview(null, null);
}

const clearActions = (setActionsPos) => {
  setActionsPos(null);
}

//
const setActionsOnBlock = (block, setActionsPos, setActionsUid) => {
  // if we have a block...
  if (block) {
    // get the bounding box
    const blockBox = block.getBoundingClientRect();
    // update the position to the top-right corner of the block
    setActionsPos({ x: blockBox.left + blockBox.width, y: blockBox.top });
    setActionsUid(block.getAttribute('data-uid'));
  } else {
    // if we don't have a block, reset actions and position
    clearActions(setActionsPos);
  }
}

// Determine whether to show block actions
// based on mousemove event target
export const updateActions = (e: React.MouseEvent<HTMLElement>, setActionsPos, isUsingActions, setActionsUid) => {

  // get details about the hovered element
  const closestBlock = getClosestBlock(e);
  if (closestBlock) {
    setActionsOnBlock(closestBlock, setActionsPos, setActionsUid);
    setActionsUid(closestBlock.getAttribute('data-uid'));
    return;
  } else if (!isUsingActions) {
    clearActions(setActionsPos);
    setActionsUid(null);
    return;
  }
}

// If I'm hovering anywhere within a block, set a class on that block
export const updateBlockHover = (e: React.MouseEvent<HTMLElement>) => {

  // get the closest block
  const targetBlock = getClosestBlock(e);

  if (targetBlock) {
    // if we're hovering over a block, add the hover class
    targetBlock.classList.add('is-hovered');
    // and remove the class when the mouse leaves the block
    targetBlock.addEventListener('mouseout', () => targetBlock.classList.remove('is-hovered'));
  }
}