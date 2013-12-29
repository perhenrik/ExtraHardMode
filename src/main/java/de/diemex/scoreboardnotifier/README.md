# ScoreBoard Notifier
by Diemex

<<<<<<< HEAD
Usually there is one scoreboard and everyone can see the same thing. This adds per player scoreboards
=======
Ever wanted to show messages without spamming the chat? This adds per player scoreboards
>>>>>>> 1c52d46... Add documentation and restore scoreboards from other plugins.
that can be used to send messages to a player without spamming the chat. It supports auto removal of
messages, handling of similar message contents, splitting of text so it fits the 16 char limit and it
will add a suffix showing the count of a msg if it is being shown multiple times.
Scoreboards from other plugins will be restored once there are no messages to be displayed anymore.

## Usage

<<<<<<< HEAD
=======
Copy these classes into your project (maven)
This package will be a separate project once it is stable and everything works perfectly.

>>>>>>> 1c52d46... Add documentation and restore scoreboards from other plugins.
Create a new ScoreboardManager

``` java
NotificationManager man = NotificationManager(plugin);
```

Send your messages via
``` java
man.showTimedPopup("Notch", 400, "Messages", "You rock!");
```