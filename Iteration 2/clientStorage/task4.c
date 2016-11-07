#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <signal.h>

#include <sys/wait.h>

#define MAXNUM 1024

//Morphing a new program
void morph(char* number) {
  char *argv[3];
  argv[0] = "task4";
  argv[1] = number;
  argv[2] = 0;
  execv("isPrime", argv);
}

//aux function.
void set_result_by_pid(pid_t *pids, int *result, int num, pid_t pid, int code) {
  int i=0;
  for (i=0; i!=num; ++i)
    if (pids[i] == pid)
      result[i] = code;
}

//aux variable
int count_finished = 0;
int count_unfinished = 0;

//signal handle function
static void sig_usr(int sig_no) {
   printf("The number of finished processes: %d\n", count_finished);
   printf("The number of unfinished processes: %d\n", count_unfinished);
}

int main(int argc, char** argv) {
  pid_t pids[MAXNUM], pid; //children's pid
  int result[MAXNUM] = {-1};  //children's return code
  int i = 0;
  int count = argc - 1;
  int status = 0;

  int stat;
  if (argc == 1) {
    printf("usage: ./%s num1 num2...\n", argv[0]);
    return 2;
  }

  //register the signal handle function
  if (signal(SIGUSR1, sig_usr) == SIG_ERR) {
      printf("can't catch SIGUSR1\n");
      return 2;
  }

  for (i=1; i!=argc; ++i) {
    if ((pid=fork()) == -1) {
      printf("fork failed\n");
      return 0;
    } else if (pid > 0) { //parent
      pids[i-1] = pid;
    } else { //childs
      morph(argv[i]);
      return 2; //never reach
    }
  }
  
  for (i=1; i!=argc; ++i) {
    pid = waitpid(-1, &status, 0); //wait children to return
    if (pid == -1) {
      return 2;
    }
    set_result_by_pid(pids, result, argc-1, pid, WEXITSTATUS(status));
    ++count_finished;
    count_unfinished = argc - 1 - count_finished;
  }

  printf("The numbers are prime: ");
  for (i=0; i!=argc-1; ++i)
    if (result[i] == 1)
      printf("%s ", argv[i+1]);
  printf("\n");

  return 0;
}

